package me.aki.tactical.conversion.smali2dex.utils;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.*;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.type.*;
import me.aki.tactical.core.utils.AbstractCfgGraph;
import me.aki.tactical.dex.DetailedDexType;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.utils.DexCfgGraph;
import me.aki.tactical.dex.utils.DexInsnVisitor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * This InsnVisitor reports for most instructions which type they expect to read from registers and which types they will write.
 *
 * It's limitations are:
 * - int/float and long/double constants cannot be distinguished
 * - moves of int/float and long/double values cannot be distinguished
 * - returns of int/float and long/double values cannot be distinguished
 * - reads and writes on int/float and long/double arrays cannot be distinguish.
 *
 * Note: The {@link TypeHintInsnVisitor#setInstruction(DexCfgGraph.Node)} method must be called before any instruction visit.
 */
public abstract class TypeHintInsnVisitor extends DexInsnVisitor<Instruction, Register> {
    /**
     * Cfg node of the instruction that is currently visited
     */
    protected DexCfgGraph.Node instruction;

    public TypeHintInsnVisitor() {
        super();
    }

    public TypeHintInsnVisitor(DexInsnVisitor<Instruction, Register> iv) {
        super(iv);
    }

    /**
     * Set the instruction that will be visited next.
     * It must be a cfg node so move-result instructions can access the previous instruction.
     *
     * @param instruction the instruction that is visited next
     */
    public void setInstruction(DexCfgGraph.Node instruction) {
        this.instruction = instruction;
    }

    protected abstract void visitRegisterWrite(Optional<Type> type, Register register);
    protected abstract void visitRegisterRead(Optional<Type> type, Register register);

    private void visitRegisterWrite(Type type, Register register) {
        visitRegisterWrite(Optional.of(type), register);
    }

    private void visitRegisterRead(Type type, Register register) {
        visitRegisterRead(Optional.of(type), register);
    }

    private Optional<Type> toType(DetailedDexType type) {
        switch (type) {
            case BOOLEAN: return Optional.of(BooleanType.getInstance());
            case BYTE: return Optional.of(ByteType.getInstance());
            case CHAR: return Optional.of(CharType.getInstance());
            case SHORT: return Optional.of(ShortType.getInstance());
            case OBJECT: return Optional.of(ObjectType.OBJECT);

            case NORMAL: // either int or float
            case WIDE: // either long or double
                return Optional.empty();

            default: return DexUtils.unreachable();
        }
    }

    private Optional<Type> toType(DexType type) {
        switch (type) {
            case NORMAL: // either int or float
            case WIDE: // either long or double
                return Optional.empty();

            case OBJECT: return Optional.of(ObjectType.OBJECT);
            default: return DexUtils.unreachable();
        }
    }

    private ObjectType arrayType() {
        return ObjectType.OBJECT;
    }

    @Override
    public void visitConstant(DexConstant constant, Register target) {
        super.visitConstant(constant, target);

        if (constant instanceof DexNumber32Constant) {
            // Type is not known: either int or float
            visitRegisterWrite(Optional.empty(), target);
        } else if (constant instanceof DexNumber64Constant) {
            // Type is not known: either long or double
            visitRegisterWrite(Optional.empty(), target);
        } else if (constant instanceof StringConstant) {
            visitRegisterWrite(ObjectType.STRING, target);
        } else if (constant instanceof ClassConstant) {
            visitRegisterWrite(ObjectType.CLASS, target);
        } else if (constant instanceof HandleConstant) {
            visitRegisterWrite(ObjectType.METHOD_HANDLE, target);
        } else if (constant instanceof MethodTypeConstant) {
            visitRegisterWrite(ObjectType.METHOD_TYPE, target);
        } else {
            DexUtils.unreachable();
        }
    }

    @Override
    public void visitAdd(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitAdd(type, op1, op2, result);

        visitMathInsn(type, op1, op2, result);
    }

    @Override
    public void visitSub(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitSub(type, op1, op2, result);

        visitMathInsn(type, op1, op2, result);
    }

    @Override
    public void visitMul(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitMul(type, op1, op2, result);

        visitMathInsn(type, op1, op2, result);
    }

    @Override
    public void visitDiv(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitDiv(type, op1, op2, result);

        visitMathInsn(type, op1, op2, result);
    }

    @Override
    public void visitMod(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitMod(type, op1, op2, result);

        visitMathInsn(type, op1, op2, result);
    }

    @Override
    public void visitAnd(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitAnd(type, op1, op2, result);

        visitMathInsn(type, op1, op2, result);
    }

    @Override
    public void visitOr(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitOr(type, op1, op2, result);

        visitMathInsn(type, op1, op2, result);
    }

    @Override
    public void visitXor(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitXor(type, op1, op2, result);

        visitMathInsn(type, op1, op2, result);
    }

    private void visitMathInsn(PrimitiveType type, Register op1, Register op2, Register result) {
        visitRegisterRead(type, op1);
        visitRegisterRead(type, op2);
        visitRegisterRead(type, result);
    }

    @Override
    public void visitShl(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitShl(type, op1, op2, result);

        visitBitShift(type, op1, op2, result);
    }

    @Override
    public void visitShr(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitShr(type, op1, op2, result);

        visitBitShift(type, op1, op2, result);
    }

    @Override
    public void visitUShr(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitUShr(type, op1, op2, result);

        visitBitShift(type, op1, op2, result);
    }

    private void visitBitShift(PrimitiveType type, Register op1, Register op2, Register result) {
        visitRegisterRead(type, op1);
        visitRegisterRead(IntType.getInstance(), op2);
        visitRegisterWrite(type, result);
    }

    @Override
    public void visitLitAdd(Register op1, short literal, Register result) {
        super.visitLitAdd(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitRSub(Register op1, short literal, Register result) {
        super.visitLitRSub(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitMul(Register op1, short literal, Register result) {
        super.visitLitMul(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitDiv(Register op1, short literal, Register result) {
        super.visitLitDiv(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitMod(Register op1, short literal, Register result) {
        super.visitLitMod(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitAnd(Register op1, short literal, Register result) {
        super.visitLitAnd(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitOr(Register op1, short literal, Register result) {
        super.visitLitOr(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitXor(Register op1, short literal, Register result) {
        super.visitLitXor(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitShl(Register op1, short literal, Register result) {
        super.visitLitShl(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitShr(Register op1, short literal, Register result) {
        super.visitLitShr(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    @Override
    public void visitLitUShr(Register op1, short literal, Register result) {
        super.visitLitUShr(op1, literal, result);

        visitLiteralMath(op1, result);
    }

    private void visitLiteralMath(Register op1, Register result) {
        visitRegisterRead(IntType.getInstance(), op1);
        visitRegisterRead(IntType.getInstance(), result);
    }

    @Override
    public void visitNeg(PrimitiveType type, Register value, Register result) {
        super.visitNeg(type, value, result);

        visitRegisterRead(type, value);
        visitRegisterWrite(type, result);
    }

    @Override
    public void visitNot(PrimitiveType type, Register value, Register result) {
        super.visitNot(type, value, result);

        visitRegisterRead(type, value);
        visitRegisterWrite(type, result);
    }

    @Override
    public void visitCmp(Register op1, Register op2, Register result) {
        super.visitCmp(op1, op2, result);

        visitRegisterRead(LongType.getInstance(), op1);
        visitRegisterRead(LongType.getInstance(), op2);
        visitRegisterWrite(IntType.getInstance(), result);
    }

    @Override
    public void visitCmpl(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitCmpl(type, op1, op2, result);

        visitRegisterRead(type, op1);
        visitRegisterRead(type, op2);
        visitRegisterWrite(IntType.getInstance(), result);
    }

    @Override
    public void visitCmpg(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitCmpg(type, op1, op2, result);

        visitRegisterRead(type, op1);
        visitRegisterRead(type, op2);
        visitRegisterWrite(IntType.getInstance(), result);
    }

    @Override
    public void visitArrayLength(Register array, Register result) {
        super.visitArrayLength(array, result);

        visitRegisterRead(arrayType(), result); // We just know that it's an array, neither the base type nor the dimensions
        visitRegisterWrite(IntType.getInstance(), result);
    }

    @Override
    public void visitArrayLoad(DetailedDexType type, Register array, Register index, Register result) {
        super.visitArrayLoad(type, array, index, result);

        visitRegisterRead(arrayType(), array);
        visitRegisterRead(IntType.getInstance(), index);
        visitRegisterWrite(toType(type), result);
    }

    @Override
    public void visitArrayStore(DetailedDexType type, Register array, Register index, Register value) {
        super.visitArrayStore(type, array, index, value);

        visitRegisterRead(arrayType(), array);
        visitRegisterRead(IntType.getInstance(), index);
        visitRegisterRead(toType(type), value);
    }

    @Override
    public void visitFillArray(Register array, FillArrayInstruction.NumberSize elementSize, List<FillArrayInstruction.NumericConstant> values) {
        super.visitFillArray(array, elementSize, values);

        visitRegisterRead(arrayType(), array);
    }

    @Override
    public void visitNewArray(ArrayType type, Register size, Register result) {
        super.visitNewArray(type, size, result);

        visitRegisterRead(IntType.getInstance(), size);
        visitRegisterWrite(type, result);
    }

    @Override
    public void visitNewFilledArray(ArrayType type, List<Register> registers) {
        super.visitNewFilledArray(type, registers);

        Type baseType = type.getBaseType();
        for (Register register : registers) {
            visitRegisterRead(baseType, register);
        }
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, Register fromRegister, Register toRegister) {
        super.visitPrimitiveCast(fromType, toType, fromRegister, toRegister);

        visitRegisterRead(fromType, fromRegister);
        visitRegisterWrite(toType, toRegister);
    }

    @Override
    public void visitRefCast(RefType type, Register register) {
        super.visitRefCast(type, register);

        visitRegisterRead(ObjectType.OBJECT, register);
    }

    @Override
    public void visitMonitorEnter(Register value) {
        super.visitMonitorEnter(value);

        visitRegisterRead(ObjectType.OBJECT, value);
    }

    @Override
    public void visitMonitorExit(Register value) {
        super.visitMonitorExit(value);

        visitRegisterRead(ObjectType.OBJECT, value);
    }

    @Override
    public void visitNew(Path type, Register result) {
        super.visitNew(type, result);

        visitRegisterWrite(new ObjectType(type), result);
    }

    @Override
    public void visitInstanceOf(RefType type, Register value, Register result) {
        super.visitInstanceOf(type, value, result);

        visitRegisterRead(ObjectType.OBJECT, value);
        visitRegisterWrite(IntType.getInstance(), result);
    }

    @Override
    public void visitReturn(DexType type, Register register) {
        super.visitReturn(type, register);

        visitRegisterRead(toType(type), register);
    }

    @Override
    public void visitReturnVoid() {
        super.visitReturnVoid();
    }

    @Override
    public void visitThrow(Register exception) {
        super.visitThrow(exception);

        visitRegisterRead(ObjectType.OBJECT, exception);
    }

    @Override
    public void visitFieldGet(FieldRef field, Optional<Register> instanceOpt, Register result) {
        super.visitFieldGet(field, instanceOpt, result);

        instanceOpt.ifPresent(instance -> visitRegisterRead(ObjectType.OBJECT, instance));
        visitRegisterWrite(field.getType(), result);
    }

    @Override
    public void visitFieldSet(FieldRef field, Optional<Register> instanceOpt, Register value) {
        super.visitFieldSet(field, instanceOpt, value);

        instanceOpt.ifPresent(instance -> visitRegisterRead(ObjectType.OBJECT, instance));
        visitRegisterRead(field.getType(), value);
    }

    @Override
    public void visitInvoke(InvokeType invoke, MethodRef method, Optional<Register> instanceOpt, List<Register> arguments) {
        super.visitInvoke(invoke, method, instanceOpt, arguments);

        instanceOpt.ifPresent(instance -> visitRegisterRead(ObjectType.OBJECT, instance));

        Iterator<Type> argumentRegisterIter = method.getArguments().iterator();
        for (Register argument : arguments) {
            visitRegisterRead(argumentRegisterIter.next(), argument);
        }
    }

    @Override
    public void visitPolymorphicInvoke(MethodRef method, MethodDescriptor descriptor, Register instance, List<Register> arguments) {
        super.visitPolymorphicInvoke(method, descriptor, instance, arguments);

        visitRegisterRead(ObjectType.OBJECT, instance);

        Iterator<Type> argumentRegisterIter = descriptor.getParameterTypes().iterator();
        for (Register argument : arguments) {
            visitRegisterRead(argumentRegisterIter.next(), argument);
        }
    }

    @Override
    public void visitCustomInvoke(List<Register> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
        super.visitCustomInvoke(arguments, name, descriptor, bootstrapArguments, bootstrapMethod);

        Iterator<Type> paramTypeIter = descriptor.getParameterTypes().iterator();
        for (Register argument : arguments) {
            visitRegisterRead(paramTypeIter.next(), argument);
        }
    }

    @Override
    public void visitMove(DexType type, Register from, Register to) {
        super.visitMove(type, from, to);

        Optional<Type> typ = toType(type);
        visitRegisterRead(typ, from);
        visitRegisterWrite(typ, to);
    }

    @Override
    public void visitMoveResult(DexType dexType, Register register) {
        super.visitMoveResult(dexType, register);

        List<DexCfgGraph.Node> predecessors = instruction.getPreceeding();
        if (predecessors.size() != 1) {
            // More than one predecessor can only exist if some instruction branches to this move-result instruction.
            // This is not allowed since move-result instructions must directly follow their value providing instruction
            throw new IllegalStateException("Illegal branch to move-result instruction");
        }

        Instruction predecessor = predecessors.iterator().next().getInstruction();
        if (predecessor instanceof InvokeInstruction) {
            Optional<Type> type = ((InvokeInstruction) predecessor).getInvoke().getDescriptor().getReturnType();
            if (!type.isPresent()) {
                throw new IllegalStateException("Cannot move result value of void");
            }

            visitRegisterWrite(type, register);
        } else if (predecessor instanceof NewFilledArrayInstruction) {
            visitRegisterWrite(arrayType(), register);
        } else {
            throw new IllegalStateException("There's nothing to move after a " + predecessor.getClass().getSimpleName());
        }
    }

    @Override
    public void visitMoveException(Register target) {
        super.visitMoveException(target);

        visitRegisterWrite(ObjectType.OBJECT, target);
    }

    @Override
    public void visitGoto(Instruction target) {
        super.visitGoto(target);
    }

    @Override
    public void visitIf(IfInstruction.Comparison comparison, Register op1, Optional<Register> op2Opt, Instruction target) {
        super.visitIf(comparison, op1, op2Opt, target);

        visitRegisterRead(IntType.getInstance(), op1);
        op2Opt.ifPresent(op2 -> visitRegisterRead(IntType.getInstance(), op2));
    }

    @Override
    public void visitSwitch(Register value, LinkedHashMap<Integer, Instruction> branchTable) {
        super.visitSwitch(value, branchTable);

        visitRegisterRead(IntType.getInstance(), value);
    }
}
