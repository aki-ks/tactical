package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.conversion.stackasm.StackInsnVisitor;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.IntConstant;
import me.aki.tactical.core.constant.PushableConstant;
import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.expr.AbstractBinaryExpr;
import me.aki.tactical.ref.expr.AddExpr;
import me.aki.tactical.ref.expr.AndExpr;
import me.aki.tactical.ref.expr.ArrayBoxExpr;
import me.aki.tactical.ref.expr.ArrayLengthExpr;
import me.aki.tactical.ref.expr.CastExpr;
import me.aki.tactical.ref.expr.CmpExpr;
import me.aki.tactical.ref.expr.CmpgExpr;
import me.aki.tactical.ref.expr.CmplExpr;
import me.aki.tactical.ref.expr.ConstantExpr;
import me.aki.tactical.ref.expr.DivExpr;
import me.aki.tactical.ref.expr.InstanceFieldExpr;
import me.aki.tactical.ref.expr.InstanceOfExpr;
import me.aki.tactical.ref.expr.InvokeExpr;
import me.aki.tactical.ref.expr.ModExpr;
import me.aki.tactical.ref.expr.MulExpr;
import me.aki.tactical.ref.expr.NegExpr;
import me.aki.tactical.ref.expr.NewArrayExpr;
import me.aki.tactical.ref.expr.NewExpr;
import me.aki.tactical.ref.expr.OrExpr;
import me.aki.tactical.ref.expr.ShlExpr;
import me.aki.tactical.ref.expr.ShrExpr;
import me.aki.tactical.ref.expr.StaticFieldExpr;
import me.aki.tactical.ref.expr.SubExpr;
import me.aki.tactical.ref.expr.UShrExpr;
import me.aki.tactical.ref.expr.XorExpr;
import me.aki.tactical.ref.invoke.AbstractInstanceInvoke;
import me.aki.tactical.ref.invoke.AbstractInvoke;
import me.aki.tactical.ref.invoke.InvokeStatic;
import me.aki.tactical.ref.stmt.AssignStatement;
import me.aki.tactical.ref.stmt.GotoStmt;
import me.aki.tactical.ref.stmt.InvokeStmt;
import me.aki.tactical.ref.stmt.MonitorEnterStmt;
import me.aki.tactical.ref.stmt.MonitorExitStmt;
import me.aki.tactical.ref.stmt.ReturnStmt;
import me.aki.tactical.ref.stmt.ThrowStmt;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.insn.IfInsn;
import me.aki.tactical.stack.insn.Instruction;
import me.aki.tactical.stack.invoke.Invoke;
import me.aki.tactical.stack.invoke.SpecialInvoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RefInsnWriter extends StackInsnVisitor<Instruction> {
    private final BodyConverter converter;

    /**
     * Instance of the current visited instruction
     */
    private Instruction instruction;

    public RefInsnWriter(BodyConverter converter) {
        super(null);
        this.converter = converter;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    /**
     * Convert an instruction or else merge the popped values.
     *
     * @param pops values popped from the stack
     * @param convertInstruction lambda that converts the instruction and returns the pushed value
     */
    private void convertOrElseMerge(List<StackValue> pops, Supplier<Optional<StackValue>> convertInstruction) {
        StackDelta delta = converter.getStackDeltaMap().get(instruction);

        if (delta == null) {
            Optional<StackValue> push = convertInstruction.get();
            delta = new StackDelta(pops, push);
            converter.getStackDeltaMap().put(instruction, delta);
        } else {
            delta.merge(converter, pops);
        }

        for (StackValue pop : pops) {
            List<Cell<Expression>> references = pop.getReferences();

            // if a value is referenced multiple times, move it to a local.
            if (references.size() > 1) {
                Expression value = pop.getValue();
                if (value instanceof ConstantExpr) {
                    // ConstantExpr can just be copied
                    PushableConstant constant = ((ConstantExpr) value).getConstant();

                    references.forEach(cell -> cell.set(new ConstantExpr(constant)));
                } else {
                    pop.storeInLocal(converter, converter.newLocal());
                }
            }
        }

        delta.getPush().ifPresent(converter::push);
    }

    @Override
    public void visitPush(PushableConstant constant) {
        convertOrElseMerge(List.of(), () ->
                Optional.of(new StackValue(instruction, new ConstantExpr(constant))));

        super.visitPush(constant);
    }

    @Override
    public void visitNeg(Type type) {
        StackValue value = converter.pop();
        convertOrElseMerge(List.of(value), () -> {
            NegExpr expr = new NegExpr(value.getValue());
            value.addReference(expr.getValueCell());

            return Optional.of(new StackValue(instruction, expr));
        });

        super.visitNeg(type);
    }

    private void convertBinaryMathExpr(BiFunction<Expression, Expression, AbstractBinaryExpr> newMathExpression) {
        StackValue value2 = converter.pop();
        StackValue value1 = converter.pop();

        convertOrElseMerge(List.of(value2, value1), () -> {
            AbstractBinaryExpr expr = newMathExpression.apply(value1.getValue(), value2.getValue());
            value1.addReference(expr.getValue1Cell());
            value2.addReference(expr.getValue2Cell());

            return Optional.of(new StackValue(instruction, expr));
        });
    }

    @Override
    public void visitAdd(Type type) {
        convertBinaryMathExpr(AddExpr::new);
        super.visitAdd(type);
    }

    @Override
    public void visitSub(Type type) {
        convertBinaryMathExpr(SubExpr::new);
        super.visitSub(type);
    }

    @Override
    public void visitMul(Type type) {
        convertBinaryMathExpr(MulExpr::new);
        super.visitMul(type);
    }

    @Override
    public void visitDiv(Type type) {
        convertBinaryMathExpr(DivExpr::new);
        super.visitDiv(type);
    }

    @Override
    public void visitMod(Type type) {
        convertBinaryMathExpr(ModExpr::new);
        super.visitMod(type);
    }

    @Override
    public void visitAnd(Type type) {
        convertBinaryMathExpr(AndExpr::new);
        super.visitAnd(type);
    }

    @Override
    public void visitOr(Type type) {
        convertBinaryMathExpr(OrExpr::new);
        super.visitOr(type);
    }

    @Override
    public void visitXor(Type type) {
        convertBinaryMathExpr(XorExpr::new);
        super.visitXor(type);
    }

    @Override
    public void visitShl(Type type) {
        convertBinaryMathExpr(ShlExpr::new);
        super.visitShl(type);
    }

    @Override
    public void visitShr(Type type) {
        convertBinaryMathExpr(ShrExpr::new);
        super.visitShr(type);
    }

    @Override
    public void visitUShr(Type type) {
        convertBinaryMathExpr(UShrExpr::new);
        super.visitUShr(type);
    }

    @Override
    public void visitCmp() {
        convertBinaryMathExpr(CmpExpr::new);
        super.visitCmp();
    }

    @Override
    public void visitCmpl(Type type) {
        convertBinaryMathExpr(CmplExpr::new);
        super.visitCmpl(type);
    }

    @Override
    public void visitCmpg(Type type) {
        convertBinaryMathExpr(CmpgExpr::new);
        super.visitCmpg(type);
    }

    @Override
    public void visitNewArray(ArrayType type, int initializedDimensions) {
        List<StackValue> dimensions = new ArrayList<>(initializedDimensions);
        for (int i = 0; i < initializedDimensions; i++) {
            dimensions.add(converter.pop());
        }

        Collections.reverse(dimensions);

        convertOrElseMerge(dimensions, () -> {
            List<Expression> dimensionSizes = dimensions.stream()
                    .map(StackValue::getValue)
                    .collect(Collectors.toList());

            NewArrayExpr expr = new NewArrayExpr(type, dimensionSizes);

            Iterator<StackValue> valueIter = dimensions.iterator();
            Iterator<Cell<Expression>> cellIter = expr.getDimensionSizeCells().iterator();
            while(valueIter.hasNext()) {
                valueIter.next().addReference(cellIter.next());
            }

            return Optional.of(new StackValue(instruction, expr));
        });
        super.visitNewArray(type, initializedDimensions);
    }

    @Override
    public void visitArrayLength() {
        StackValue array = converter.pop();

        convertOrElseMerge(List.of(array), () -> {
            ArrayLengthExpr expr = new ArrayLengthExpr(array.getValue());

            array.addReference(expr.getArrayCell());

            return Optional.of(new StackValue(instruction, expr));
        });

        super.visitArrayLength();
    }

    @Override
    public void visitArrayLoad(Type type) {
        StackValue index = converter.pop();
        StackValue array = converter.pop();

        convertOrElseMerge(List.of(index, array), () -> {
            ArrayBoxExpr expr = new ArrayBoxExpr(array.getValue(), index.getValue());

            array.addReference(expr.getArrayCell());
            index.addReference(expr.getIndexCell());

            return Optional.of(new StackValue(instruction, expr));
        });

        super.visitArrayLoad(type);
    }

    @Override
    public void visitArrayStore(Type type) {
        StackValue value = converter.pop();
        StackValue index = converter.pop();
        StackValue array = converter.pop();

        convertOrElseMerge(List.of(value, index, array), () -> {
            ArrayBoxExpr expr = new ArrayBoxExpr(array.getValue(), index.getValue());
            AssignStatement statement = new AssignStatement(expr, value.getValue());

            array.addReference(expr.getArrayCell());
            index.addReference(expr.getIndexCell());
            value.addReference(statement.getValueCell());

            converter.addStatement(instruction, statement);
            return Optional.empty();
        });

        super.visitArrayStore(type);
    }

    @Override
    public void visitSwap() {
        StackValue value1 = converter.pop();
        StackValue value2 = converter.pop();
        converter.push(value1);
        converter.push(value2);

        super.visitSwap();
    }

    @Override
    public void visitPop() {
        converter.pop();

        super.visitPop();
    }

    @Override
    public void visitDup() {
        StackValue value = converter.pop();
        converter.push(value);
        converter.push(value);

        super.visitDup();
    }

    @Override
    public void visitDupX1() {
        StackValue value1 = converter.pop();
        StackValue value2 = converter.pop();

        converter.push(value1);

        converter.push(value2);
        converter.push(value1);

        super.visitDupX1();
    }

    @Override
    public void visitDupX2() {
        StackValue value1 = converter.pop();
        StackValue value2 = converter.pop();
        StackValue value3 = converter.pop();

        converter.push(value1);

        converter.push(value3);
        converter.push(value2);
        converter.push(value1);

        super.visitDupX2();
    }

    @Override
    public void visitDup2() {
        StackValue value1 = converter.pop();
        StackValue value2 = converter.pop();

        converter.push(value2);
        converter.push(value1);

        converter.push(value2);
        converter.push(value1);

        super.visitDup2();
    }

    @Override
    public void visitDup2X1() {
        StackValue value1 = converter.pop();
        StackValue value2 = converter.pop();
        StackValue value3 = converter.pop();

        converter.push(value2);
        converter.push(value1);

        converter.push(value3);
        converter.push(value2);
        converter.push(value1);

        super.visitDup2X1();
    }

    @Override
    public void visitDup2X2() {
        StackValue value1 = converter.pop();
        StackValue value2 = converter.pop();
        StackValue value3 = converter.pop();
        StackValue value4 = converter.pop();

        converter.push(value2);
        converter.push(value1);

        converter.push(value4);
        converter.push(value3);
        converter.push(value2);
        converter.push(value1);

        super.visitDup2X2();
    }

    @Override
    public void visitLoad(Type type, StackLocal stackLocal) {
        convertOrElseMerge(List.of(), () -> {
            RefLocal refLocal = converter.getLocal(stackLocal);
            return Optional.of(new StackValue(instruction, refLocal));
        });

        super.visitLoad(type, stackLocal);
    }

    @Override
    public void visitStore(Type type, StackLocal stackLocal) {
        StackValue value = converter.pop();

        convertOrElseMerge(List.of(value), () -> {
            RefLocal refLocal = converter.getLocal(stackLocal);
            AssignStatement statement = new AssignStatement(refLocal, value.getValue());

            value.addReference(statement.getValueCell());

            converter.addStatement(instruction, statement);
            return Optional.empty();
        });

        super.visitStore(type, stackLocal);
    }

    @Override
    public void visitIncrement(StackLocal stackLocal, int value) {
        convertOrElseMerge(List.of(), () -> {
            RefLocal refLocal = converter.getLocal(stackLocal);
            AssignStatement statement = new AssignStatement(refLocal, new AddExpr(refLocal, new ConstantExpr(new IntConstant(1))));

            converter.addStatement(instruction, statement);
            return Optional.empty();
        });

        super.visitIncrement(stackLocal, value);
    }

    @Override
    public void visitNew(Path type) {
        convertOrElseMerge(List.of(), () -> {
            NewExpr expr = new NewExpr(type);

            return Optional.of(new StackValue(instruction, expr));
        });

        super.visitNew(type);
    }

    @Override
    public void visitInstanceOf(RefType type) {
        StackValue value = converter.pop();

        convertOrElseMerge(List.of(value), () -> {
            InstanceOfExpr expr = new InstanceOfExpr(type, value.getValue());

            value.addReference(expr.getValueCell());

            return Optional.of(new StackValue(instruction, expr));
        });

        super.visitInstanceOf(type);
    }

    private void visitCast(Type type) {
        StackValue value = converter.pop();

        convertOrElseMerge(List.of(value), () -> {
            CastExpr expr = new CastExpr(type, value.getValue());

            value.addReference(expr.getValueCell());

            return Optional.of(new StackValue(instruction, expr));
        });
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType from, PrimitiveType to) {
        visitCast(to);

        super.visitPrimitiveCast(from, to);
    }

    @Override
    public void visitReferenceCast(RefType type) {
        visitCast(type);

        super.visitReferenceCast(type);
    }

    @Override
    public void visitReturn(Optional<Type> type) {
        Optional<StackValue> valueOpt = type.map(x -> converter.pop());

        convertOrElseMerge(valueOpt.map(List::of).orElseGet(List::of), () -> {
            ReturnStmt stmt = new ReturnStmt(valueOpt.map(StackValue::getValue));

            valueOpt.ifPresent(value -> value.addReference(stmt.getValueCell().get()));

            converter.addStatement(instruction, stmt);
            return Optional.empty();
        });

        super.visitReturn(type);
    }

    @Override
    public void visitThrow() {
        StackValue exception = converter.pop();

        convertOrElseMerge(List.of(exception), () -> {
            ThrowStmt stmt = new ThrowStmt(exception.getValue());

            exception.addReference(stmt.getValueCell());

            converter.addStatement(instruction, stmt);
            return Optional.empty();
        });

        super.visitThrow();
    }

    @Override
    public void visitMonitorEnter() {
        StackValue value = converter.pop();

        convertOrElseMerge(List.of(value), () -> {
            MonitorEnterStmt stmt = new MonitorEnterStmt(value.getValue());

            value.addReference(stmt.getValueCell());

            converter.addStatement(instruction, stmt);
            return Optional.empty();
        });

        super.visitMonitorEnter();
    }

    @Override
    public void visitMonitorExit() {
        StackValue value = converter.pop();

        convertOrElseMerge(List.of(value), () -> {
            MonitorExitStmt stmt = new MonitorExitStmt(value.getValue());

            value.addReference(stmt.getValueCell());

            converter.addStatement(instruction, stmt);
            return Optional.empty();
        });

        super.visitMonitorExit();
    }

    @Override
    public void visitFieldGet(FieldRef fieldRef, boolean isStatic) {
        if (isStatic) {
            convertOrElseMerge(List.of(), () -> {
                StaticFieldExpr expr = new StaticFieldExpr(fieldRef);
                return Optional.of(new StackValue(instruction, expr));
            });
        } else {
            StackValue instance = converter.pop();
            convertOrElseMerge(List.of(instance), () -> {
                InstanceFieldExpr expr = new InstanceFieldExpr(fieldRef, instance.getValue());

                instance.addReference(expr.getInstanceCell());

                return Optional.of(new StackValue(instruction, expr));
            });
        }

        super.visitFieldGet(fieldRef, isStatic);
    }

    @Override
    public void visitFieldSet(FieldRef fieldRef, boolean isStatic) {
        StackValue value = converter.pop();

        if (isStatic) {
            convertOrElseMerge(List.of(value), () -> {
                StaticFieldExpr expr = new StaticFieldExpr(fieldRef);
                AssignStatement stmt = new AssignStatement(expr, value.getValue());

                value.addReference(stmt.getValueCell());

                converter.addStatement(instruction, stmt);
                return Optional.empty();
            });
        } else {
            StackValue instance = converter.pop();

            convertOrElseMerge(List.of(value, instance), () -> {
                InstanceFieldExpr expr = new InstanceFieldExpr(fieldRef, instance.getValue());
                AssignStatement stmt = new AssignStatement(expr, value.getValue());

                instance.addReference(expr.getInstanceCell());
                value.addReference(stmt.getValueCell());

                converter.addStatement(instruction, stmt);
                return Optional.empty();
            });
        }

        super.visitFieldSet(fieldRef, isStatic);
    }

    @Override
    public void visitInvokeInsn(Invoke stackInvoke) {
        MethodDescriptor descriptor = stackInvoke.getDescriptor();

        List<StackValue> argumentValues = descriptor.getParameterTypes().stream()
                .map(x -> converter.pop())
                .collect(Collectors.toList());
        Collections.reverse(argumentValues);

        Optional<StackValue> instanceOpt = stackInvoke instanceof me.aki.tactical.stack.invoke.AbstractInstanceInvoke ?
                Optional.of(converter.pop()) : Optional.empty();

        List<StackValue> allPops = new ArrayList<>(argumentValues);
        instanceOpt.ifPresent(allPops::add);
        convertOrElseMerge(allPops, () -> {
            AbstractInvoke refInvoke = convertAbstractInvoke(stackInvoke, argumentValues, instanceOpt);

            if (refInvoke instanceof me.aki.tactical.ref.invoke.AbstractInstanceInvoke) {
                Cell<Expression> instanceCell = ((AbstractInstanceInvoke) refInvoke).getInstanceCell();
                instanceOpt.get().addReference(instanceCell);
            }

            Iterator<StackValue> argIter = argumentValues.iterator();
            Iterator<Cell<Expression>> cellIter = refInvoke.getArgumentCells().iterator();
            while(argIter.hasNext()) {
                argIter.next().addReference(cellIter.next());
            }

            if (descriptor.getReturnType().isPresent()) {
                RefLocal local = converter.newLocal();
                AssignStatement stmt = new AssignStatement(local, new InvokeExpr(refInvoke));
                converter.addStatement(instruction, stmt);
                return Optional.of(new StackValue(instruction, local));
            } else {
                InvokeStmt stmt = new InvokeStmt(refInvoke);
                converter.addStatement(instruction, stmt);
                return Optional.empty();
            }
        });

        super.visitInvokeInsn(stackInvoke);
    }

    private AbstractInvoke convertAbstractInvoke(Invoke stackInvoke, List<StackValue> argumentValues, Optional<StackValue> instanceOpt) {
        List<Expression> arguments = argumentValues.stream()
                .map(StackValue::getValue)
                .collect(Collectors.toList());

        if (stackInvoke instanceof me.aki.tactical.stack.invoke.AbstractInstanceInvoke) {
            if (stackInvoke instanceof me.aki.tactical.stack.invoke.InterfaceInvoke) {
                me.aki.tactical.stack.invoke.InterfaceInvoke interfaceInvoke = (me.aki.tactical.stack.invoke.InterfaceInvoke) stackInvoke;
                return new me.aki.tactical.ref.invoke.InvokeInterface(interfaceInvoke.getMethod(), instanceOpt.get().getValue(), arguments);
            } else if (stackInvoke instanceof me.aki.tactical.stack.invoke.VirtualInvoke) {
                me.aki.tactical.stack.invoke.VirtualInvoke virtualInvoke = (me.aki.tactical.stack.invoke.VirtualInvoke) stackInvoke;
                return new me.aki.tactical.ref.invoke.InvokeVirtual(virtualInvoke.getMethod(), instanceOpt.get().getValue(), arguments);
            } else if (stackInvoke instanceof SpecialInvoke) {
                SpecialInvoke specialInvoke = (SpecialInvoke) stackInvoke;
                return new me.aki.tactical.ref.invoke.InvokeSpecial(specialInvoke.getMethod(), instanceOpt.get().getValue(), arguments, specialInvoke.isInterface());
            } else {
                throw new AssertionError();
            }
        } else if (stackInvoke instanceof me.aki.tactical.stack.invoke.StaticInvoke) {
            me.aki.tactical.stack.invoke.StaticInvoke staticInvoke = (me.aki.tactical.stack.invoke.StaticInvoke) stackInvoke;
            return new InvokeStatic(staticInvoke.getMethod(), arguments, staticInvoke.isInterface());
        } else if (stackInvoke instanceof me.aki.tactical.stack.invoke.DynamicInvoke) {
            me.aki.tactical.stack.invoke.DynamicInvoke dynamicInvoke = (me.aki.tactical.stack.invoke.DynamicInvoke) stackInvoke;
            return new me.aki.tactical.ref.invoke.InvokeDynamic(
                    dynamicInvoke.getName(), dynamicInvoke.getDescriptor(), dynamicInvoke.getBootstrapMethod(),
                    dynamicInvoke.getBootstrapArguments(), arguments);
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public void visitGoto(Instruction target) {
        super.visitGoto(target);
    }

    @Override
    public void visitIf(IfInsn.Condition condition, Instruction target) {
        super.visitIf(condition, target);
    }

    @Override
    public void visitSwitch(Map<Integer, Instruction> targetTable, Instruction defaultTarget) {
        super.visitSwitch(targetTable, defaultTarget);
    }
}
