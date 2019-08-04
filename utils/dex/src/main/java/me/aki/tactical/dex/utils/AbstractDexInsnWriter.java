package me.aki.tactical.dex.utils;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.ArrayLengthInstruction;
import me.aki.tactical.dex.insn.ArrayLoadInstruction;
import me.aki.tactical.dex.insn.ArrayStoreInstruction;
import me.aki.tactical.dex.insn.CmpInstruction;
import me.aki.tactical.dex.insn.CmpgInstruction;
import me.aki.tactical.dex.insn.CmplInstruction;
import me.aki.tactical.dex.insn.ConstInstruction;
import me.aki.tactical.dex.insn.FieldGetInstruction;
import me.aki.tactical.dex.insn.FieldSetInstruction;
import me.aki.tactical.dex.insn.FillArrayInstruction;
import me.aki.tactical.dex.insn.GotoInstruction;
import me.aki.tactical.dex.insn.IfInstruction;
import me.aki.tactical.dex.insn.InstanceOfInstruction;
import me.aki.tactical.dex.insn.Instruction;
import me.aki.tactical.dex.insn.InvokeInstruction;
import me.aki.tactical.dex.insn.MonitorEnterInstruction;
import me.aki.tactical.dex.insn.MonitorExitInstruction;
import me.aki.tactical.dex.insn.MoveExceptionInstruction;
import me.aki.tactical.dex.insn.MoveInstruction;
import me.aki.tactical.dex.insn.MoveResultInstruction;
import me.aki.tactical.dex.insn.NegInstruction;
import me.aki.tactical.dex.insn.NewArrayInstruction;
import me.aki.tactical.dex.insn.NewFilledArrayInstruction;
import me.aki.tactical.dex.insn.NewInstanceInstruction;
import me.aki.tactical.dex.insn.NotInstruction;
import me.aki.tactical.dex.insn.PrimitiveCastInstruction;
import me.aki.tactical.dex.insn.RefCastInstruction;
import me.aki.tactical.dex.insn.ReturnInstruction;
import me.aki.tactical.dex.insn.ReturnVoidInstruction;
import me.aki.tactical.dex.insn.SwitchInstruction;
import me.aki.tactical.dex.insn.ThrowInstruction;
import me.aki.tactical.dex.insn.litmath.AddLitInstruction;
import me.aki.tactical.dex.insn.litmath.AndLitInstruction;
import me.aki.tactical.dex.insn.litmath.DivLitInstruction;
import me.aki.tactical.dex.insn.litmath.ModLitInstruction;
import me.aki.tactical.dex.insn.litmath.MulLitInstruction;
import me.aki.tactical.dex.insn.litmath.OrLitInstruction;
import me.aki.tactical.dex.insn.litmath.RSubLitInstruction;
import me.aki.tactical.dex.insn.litmath.ShlLitInstruction;
import me.aki.tactical.dex.insn.litmath.ShrLitInstruction;
import me.aki.tactical.dex.insn.litmath.UShrLitInstruction;
import me.aki.tactical.dex.insn.litmath.XorLitInstruction;
import me.aki.tactical.dex.insn.math.AddInstruction;
import me.aki.tactical.dex.insn.math.AndInstruction;
import me.aki.tactical.dex.insn.math.DivInstruction;
import me.aki.tactical.dex.insn.math.ModInstruction;
import me.aki.tactical.dex.insn.math.MulInstruction;
import me.aki.tactical.dex.insn.math.OrInstruction;
import me.aki.tactical.dex.insn.math.ShlInstruction;
import me.aki.tactical.dex.insn.math.ShrInstruction;
import me.aki.tactical.dex.insn.math.SubInstruction;
import me.aki.tactical.dex.insn.math.UShrInstruction;
import me.aki.tactical.dex.insn.math.XorInstruction;
import me.aki.tactical.dex.invoke.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Common superclass for {@link DexInsnVisitor DexInsnVisitors} that collect all called events
 * represented as {@link Instruction Instructions}.
 *
 * @param <I>
 * @param <R>
 */
public abstract class AbstractDexInsnWriter<I, R> extends DexInsnVisitor<I, R> {
    /**
     * All visited events are collected into this list of {@link Instruction Instructions}.
     */
    private final List<Instruction> instructions = new ArrayList<>();

    public AbstractDexInsnWriter() {
        super(null);
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public List<Instruction> popInstructions() {
        List<Instruction> currentList = new ArrayList<>(this.instructions);
        this.instructions.clear();
        return currentList;
    }

    private void visitInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public abstract Register convertRegister(R register);

    public abstract void registerReference(I instruction, RWCell<Instruction> insnCell);

    @Override
    public void visitConstant(DexConstant constant, R target) {
        visitInstruction(new ConstInstruction(constant, convertRegister(target)));
    }

    @Override
    public void visitAdd(R op1, R op2, R result) {
        visitInstruction(new AddInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitSub(R op1, R op2, R result) {
        visitInstruction(new SubInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitMul(R op1, R op2, R result) {
        visitInstruction(new MulInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitDiv(R op1, R op2, R result) {
        visitInstruction(new DivInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitMod(R op1, R op2, R result) {
        visitInstruction(new ModInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitAnd(R op1, R op2, R result) {
        visitInstruction(new AndInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitOr(R op1, R op2, R result) {
        visitInstruction(new OrInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitXor(R op1, R op2, R result) {
        visitInstruction(new XorInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitShl(R op1, R op2, R result) {
        visitInstruction(new ShlInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitShr(R op1, R op2, R result) {
        visitInstruction(new ShrInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitUShr(R op1, R op2, R result) {
        visitInstruction(new UShrInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitLitAdd(R op1, short literal, R result) {
        visitInstruction(new AddLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitRSub(R op1, short literal, R result) {
        visitInstruction(new RSubLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitMul(R op1, short literal, R result) {
        visitInstruction(new MulLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitDiv(R op1, short literal, R result) {
        visitInstruction(new DivLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitMod(R op1, short literal, R result) {
        visitInstruction(new ModLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitAnd(R op1, short literal, R result) {
        visitInstruction(new AndLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitOr(R op1, short literal, R result) {
        visitInstruction(new OrLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitXor(R op1, short literal, R result) {
        visitInstruction(new XorLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitShl(R op1, short literal, R result) {
        visitInstruction(new ShlLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitShr(R op1, short literal, R result) {
        visitInstruction(new ShrLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitLitUShr(R op1, short literal, R result) {
        visitInstruction(new UShrLitInstruction(convertRegister(op1), literal, convertRegister(result)));
    }

    @Override
    public void visitNeg(R value, R result) {
        visitInstruction(new NegInstruction(convertRegister(value), convertRegister(result)));
    }

    @Override
    public void visitNot(R value, R result) {
        visitInstruction(new NotInstruction(convertRegister(value), convertRegister(result)));
    }

    @Override
    public void visitCmp(R op1, R op2, R result) {
        visitInstruction(new CmpInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitCmpl(R op1, R op2, R result) {
        visitInstruction(new CmplInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitCmpg(R op1, R op2, R result) {
        visitInstruction(new CmpgInstruction(convertRegister(op1), convertRegister(op2), convertRegister(result)));
    }

    @Override
    public void visitArrayLength(R array, R result) {
        visitInstruction(new ArrayLengthInstruction(convertRegister(array), convertRegister(result)));
    }

    @Override
    public void visitArrayLoad(R array, R index, R result) {
        visitInstruction(new ArrayLoadInstruction(convertRegister(array), convertRegister(index), convertRegister(result)));
    }

    @Override
    public void visitArrayStore(R array, R index, R value) {
        visitInstruction(new ArrayStoreInstruction(convertRegister(array), convertRegister(index), convertRegister(value)));
    }

    @Override
    public void visitFillArray(R array, FillArrayInstruction.NumberSize elementSize, List<FillArrayInstruction.NumericConstant> values) {
        visitInstruction(new FillArrayInstruction(convertRegister(array), elementSize, values));
    }

    @Override
    public void visitNewArray(ArrayType type, R size, R result) {
        visitInstruction(new NewArrayInstruction(type, convertRegister(size), convertRegister(result)));
    }

    @Override
    public void visitNewFilledArray(ArrayType type, List<R> registers) {
        List<Register> convertedRegisters = registers.stream()
                .map(this::convertRegister)
                .collect(Collectors.toList());

        visitInstruction(new NewFilledArrayInstruction(type, convertedRegisters));
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, R fromRegister, R toRegister) {
        visitInstruction(new PrimitiveCastInstruction(fromType, toType, convertRegister(fromRegister), convertRegister(toRegister)));
    }

    @Override
    public void visitRefCast(RefType type, R register) {
        visitInstruction(new RefCastInstruction(type, convertRegister(register)));
    }

    @Override
    public void visitMonitorEnter(R value) {
        visitInstruction(new MonitorEnterInstruction(convertRegister(value)));
    }

    @Override
    public void visitMonitorExit(R value) {
        visitInstruction(new MonitorExitInstruction(convertRegister(value)));
    }

    @Override
    public void visitNew(Path type, R result) {
        visitInstruction(new NewInstanceInstruction(type, convertRegister(result)));
    }

    @Override
    public void visitInstanceOf(RefType type, R value, R result) {
        visitInstruction(new InstanceOfInstruction(type, convertRegister(value), convertRegister(result)));
    }

    @Override
    public void visitReturn(R register) {
        visitInstruction(new ReturnInstruction(convertRegister(register)));
    }

    @Override
    public void visitReturnVoid() {
        visitInstruction(new ReturnVoidInstruction());
    }

    @Override
    public void visitThrow(R exception) {
        visitInstruction(new ThrowInstruction(convertRegister(exception)));
    }

    @Override
    public void visitFieldGet(FieldRef field, Optional<R> instance, R result) {
        visitInstruction(new FieldGetInstruction(field, instance.map(this::convertRegister), convertRegister(result)));
    }

    @Override
    public void visitFieldSet(FieldRef field, Optional<R> instance, R value) {
        visitInstruction(new FieldSetInstruction(field, instance.map(this::convertRegister), convertRegister(value)));
    }

    @Override
    public void visitInvoke(InvokeType invokeType, MethodRef method, Optional<R> instance, List<R> arguments) {
        List<Register> argumentRegisters = arguments.stream()
                .map(this::convertRegister)
                .collect(Collectors.toList());

        Invoke invoke;
        if (invokeType == InvokeType.STATIC) {
            invoke = new InvokeStatic(method, argumentRegisters);
        } else {
            Register instanceRegister = convertRegister(instance.get());
            switch (invokeType) {
                case DIRECT:
                    invoke = new InvokeDirect(method, instanceRegister, argumentRegisters);
                    break;

                case INTERFACE:
                    invoke = new InvokeInterface(method, instanceRegister, argumentRegisters);
                    break;

                case SUPER:
                    invoke = new InvokeSuper(method, instanceRegister, argumentRegisters);
                    break;

                case VIRTUAL:
                    invoke = new InvokeVirtual(method, instanceRegister, argumentRegisters);
                    break;

                default:
                    throw new AssertionError();
            }
        }

        visitInstruction(new InvokeInstruction(invoke));
    }

    @Override
    public void visitPolymorphicInvoke(MethodRef method, MethodDescriptor descriptor, R instance, List<R> arguments) {
        List<Register> argumentRegisters = arguments.stream()
                .map(this::convertRegister)
                .collect(Collectors.toList());

        visitInstruction(new InvokeInstruction(new InvokePolymorphic(method, descriptor, convertRegister(instance), argumentRegisters)));
    }

    @Override
    public void visitCustomInvoke(List<R> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
        List<Register> argumentRegisters = arguments.stream()
                .map(this::convertRegister)
                .collect(Collectors.toList());

        visitInstruction(new InvokeInstruction(new InvokeCustom(argumentRegisters, name, descriptor, bootstrapArguments, bootstrapMethod)));
    }

    @Override
    public void visitMove(R from, R to) {
        visitInstruction(new MoveInstruction(convertRegister(from), convertRegister(to)));
    }

    @Override
    public void visitMoveResult(R register) {
        visitInstruction(new MoveResultInstruction(convertRegister(register)));
    }

    @Override
    public void visitMoveException(R target) {
        visitInstruction(new MoveExceptionInstruction(convertRegister(target)));
    }

    @Override
    public void visitGoto(I target) {
        GotoInstruction instruction = new GotoInstruction(null);

        registerReference(target, instruction.getTargetCell());

        visitInstruction(instruction);
    }

    @Override
    public void visitIf(IfInstruction.Comparison comparison, R op1, Optional<R> op2, I target) {
        IfInstruction instruction = new IfInstruction(comparison, convertRegister(op1), op2.map(this::convertRegister), null);

        registerReference(target, instruction.getTargetCell());

        visitInstruction(instruction);
    }

    @Override
    public void visitSwitch(R value, LinkedHashMap<Integer, I> branchTable) {
        LinkedHashMap<Integer, Instruction> table = new LinkedHashMap<>();
        SwitchInstruction instruction = new SwitchInstruction(convertRegister(value), table);

        branchTable.keySet().forEach(key -> {
            table.put(key, null);
            registerReference(branchTable.get(key), RWCell.ofMap(key, table, Instruction.class));
        });

        visitInstruction(instruction);
    }
}
