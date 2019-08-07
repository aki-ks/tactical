package me.aki.tactical.conversion.smali2dex.typing;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.*;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.type.*;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.utils.DexCfgGraph;
import me.aki.tactical.dex.utils.DexInsnVisitor;

import java.util.*;

/**
 * This InsnVisitor reports for most instructions which type they expect to read from registers and which types they will write.
 *
 * It's limitations are:
 * - int/float/long/double constants cannot be distinguished
 * - moves of int/float/long/double values cannot be distinguished
 * - returns of int/float/long/double values cannot be distinguished
 * - reads and writes on int/float/long/double arrays cannot be distinguished
 *
 * Note: The {@link TypeHintInsnVisitor#setInstruction(DexCfgGraph.Node)} method must be called before any instruction visit.
 */
public abstract class TypeHintInsnVisitor extends DexInsnVisitor<Instruction, Register> {
    /**
     * Return type of the method containing the visited instructions
     */
    private final Optional<Type> returnType;

    /**
     * Cfg node of the instruction that is currently visited
     */
    protected DexCfgGraph.Node instruction;

    public TypeHintInsnVisitor(Optional<Type> returnType) {
        this.returnType = returnType;
    }

    public TypeHintInsnVisitor(Optional<Type> returnType, DexInsnVisitor<Instruction, Register> iv) {
        super(iv);
        this.returnType = returnType;
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

    protected abstract void visit(RegisterAccess action);

    private RefType arrayType() {
        // We just know that it's an array, neither the base type nor the dimension count
        return ObjectType.OBJECT;
    }

    @Override
    public void visitConstant(DexConstant constant, Register target) {
        super.visitConstant(constant, target);

        Optional<Type> type;
        if (constant instanceof UntypedNumberConstant) {
            type = Optional.empty();
        } else if (constant instanceof NullConstant) {
            type = Optional.of(ObjectType.OBJECT);
        } else if (constant instanceof IntConstant) {
            type = Optional.of(IntType.getInstance());
        } else if (constant instanceof LongConstant) {
            type = Optional.of(LongType.getInstance());
        } else if (constant instanceof FloatConstant) {
            type = Optional.of(FloatType.getInstance());
        } else if (constant instanceof DoubleConstant) {
            type = Optional.of(DoubleType.getInstance());
        } else if (constant instanceof StringConstant) {
            type = Optional.of(ObjectType.STRING);
        } else if (constant instanceof ClassConstant) {
            type = Optional.of(ObjectType.CLASS);
        } else if (constant instanceof HandleConstant) {
            type = Optional.of(ObjectType.METHOD_HANDLE);
        } else if (constant instanceof MethodTypeConstant) {
            type = Optional.of(ObjectType.METHOD_TYPE);
        } else {
            type = DexUtils.unreachable();
        }

        visit(new RegisterAccess().withWrite(target, type));
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
        visit(new RegisterAccess()
                .withRead(op1, type)
                .withRead(op2, type)
                .withWrite(result, type));
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
        visit(new RegisterAccess()
                .withRead(op1, type)
                .withRead(op2, IntType.getInstance())
                .withWrite(result, type));
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
        visit(new RegisterAccess()
                .withRead(op1, IntType.getInstance())
                .withWrite(result, IntType.getInstance()));
    }

    @Override
    public void visitNeg(PrimitiveType type, Register value, Register result) {
        super.visitNeg(type, value, result);

        visit(new RegisterAccess()
                .withRead(value, type)
                .withWrite(result, type));
    }

    @Override
    public void visitNot(PrimitiveType type, Register value, Register result) {
        super.visitNot(type, value, result);

        visit(new RegisterAccess()
                .withRead(value, type)
                .withWrite(result, type));
    }

    @Override
    public void visitCmp(Register op1, Register op2, Register result) {
        super.visitCmp(op1, op2, result);

        visit(new RegisterAccess()
                .withRead(op1, LongType.getInstance())
                .withRead(op1, LongType.getInstance())
                .withWrite(result, IntType.getInstance()));
    }

    @Override
    public void visitCmpl(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitCmpl(type, op1, op2, result);

        visit(new RegisterAccess()
                .withRead(op1, type)
                .withRead(op1, type)
                .withWrite(result, IntType.getInstance()));
    }

    @Override
    public void visitCmpg(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitCmpg(type, op1, op2, result);

        visit(new RegisterAccess()
                .withRead(op1, type)
                .withRead(op1, type)
                .withWrite(result, IntType.getInstance()));
    }

    @Override
    public void visitArrayLength(Register array, Register result) {
        super.visitArrayLength(array, result);

        visit(new RegisterAccess()
                .withRead(result, arrayType())
                .withWrite(result, IntType.getInstance()));
    }

    @Override
    public void visitArrayLoad(Type type, Register array, Register index, Register result) {
        super.visitArrayLoad(type, array, index, result);

        visit(new RegisterAccess()
                .withRead(array, arrayType())
                .withRead(index, IntType.getInstance())
                .withWrite(result, type));
    }

    @Override
    public void visitArrayStore(Type type, Register array, Register index, Register value) {
        super.visitArrayStore(type, array, index, value);

        visit(new RegisterAccess()
                .withRead(array, arrayType())
                .withRead(index, IntType.getInstance())
                .withRead(value, type));
    }

    @Override
    public void visitFillArray(Register array, FillArrayInstruction.NumberSize elementSize, List<FillArrayInstruction.NumericConstant> values) {
        super.visitFillArray(array, elementSize, values);

        visit(new RegisterAccess().withRead(array, arrayType()));
    }

    @Override
    public void visitNewArray(ArrayType type, Register size, Register result) {
        super.visitNewArray(type, size, result);

        visit(new RegisterAccess()
                .withRead(size, IntType.getInstance())
                .withWrite(result, type));
    }

    @Override
    public void visitNewFilledArray(ArrayType type, List<Register> registers) {
        super.visitNewFilledArray(type, registers);

        RegisterAccess action = new RegisterAccess();
        Type baseType = type.getBaseType();
        for (Register register : registers) {
            action.withRead(register, baseType);
        }
        visit(action);
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, Register fromRegister, Register toRegister) {
        super.visitPrimitiveCast(fromType, toType, fromRegister, toRegister);

        visit(new RegisterAccess()
                .withRead(fromRegister, fromType)
                .withWrite(toRegister, toType));
    }

    @Override
    public void visitRefCast(RefType type, Register register) {
        super.visitRefCast(type, register);

        visit(new RegisterAccess()
                .withRead(register, type)
                .withWrite(register, type));
    }

    @Override
    public void visitMonitorEnter(Register value) {
        super.visitMonitorEnter(value);

        visit(new RegisterAccess().withRead(value, ObjectType.OBJECT));
    }

    @Override
    public void visitMonitorExit(Register value) {
        super.visitMonitorExit(value);

        visit(new RegisterAccess().withRead(value, ObjectType.OBJECT));
    }

    @Override
    public void visitNew(Path type, Register result) {
        super.visitNew(type, result);

        visit(new RegisterAccess().withWrite(result, new ObjectType(type)));
    }

    @Override
    public void visitInstanceOf(RefType type, Register value, Register result) {
        super.visitInstanceOf(type, value, result);

        visit(new RegisterAccess()
                .withRead(value, ObjectType.OBJECT)
                .withWrite(result, IntType.getInstance()));
    }

    @Override
    public void visitReturn(Register register) {
        super.visitReturn(register);

        visit(new RegisterAccess().withRead(register, returnType.orElseThrow(IllegalStateException::new)));
    }

    @Override
    public void visitReturnVoid() {
        super.visitReturnVoid();

        visit(new RegisterAccess());
    }

    @Override
    public void visitThrow(Register exception) {
        super.visitThrow(exception);

        visit(new RegisterAccess().withRead(exception, ObjectType.THROWABLE));
    }

    @Override
    public void visitFieldGet(FieldRef field, Optional<Register> instanceOpt, Register result) {
        super.visitFieldGet(field, instanceOpt, result);

        RegisterAccess action = new RegisterAccess();
        instanceOpt.ifPresent(instance -> action.withRead(instance, ObjectType.OBJECT));
        action.withWrite(result, field.getType());
        visit(action);
    }

    @Override
    public void visitFieldSet(FieldRef field, Optional<Register> instanceOpt, Register value) {
        super.visitFieldSet(field, instanceOpt, value);

        RegisterAccess action = new RegisterAccess();
        instanceOpt.ifPresent(instance -> action.withRead(instance, ObjectType.OBJECT));
        action.withRead(value, field.getType());
        visit(action);
    }

    @Override
    public void visitInvoke(InvokeType invoke, MethodRef method, Optional<Register> instanceOpt, List<Register> arguments) {
        super.visitInvoke(invoke, method, instanceOpt, arguments);

        RegisterAccess action = new RegisterAccess();
        instanceOpt.ifPresent(instance -> action.withRead(instance, ObjectType.OBJECT));

        Iterator<Type> argumentRegisterIter = method.getArguments().iterator();
        for (Register argument : arguments) {
            action.withRead(argument, argumentRegisterIter.next());
        }

        visit(action);
    }

    @Override
    public void visitCustomInvoke(List<Register> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
        super.visitCustomInvoke(arguments, name, descriptor, bootstrapArguments, bootstrapMethod);

        RegisterAccess action = new RegisterAccess();
        Iterator<Type> paramTypeIter = descriptor.getParameterTypes().iterator();
        for (Register argument : arguments) {
            action.withRead(argument, paramTypeIter.next());
        }
        visit(action);
    }

    @Override
    public void visitMove(Type type, Register from, Register to) {
        super.visitMove(type, from, to);

        // `type` may be null if it was not yet resolved
        Optional<Type> typeOpt = Optional.ofNullable(type);

        visit(new RegisterAccess()
                .withRead(from, typeOpt)
                .withWrite(to, typeOpt));
    }

    @Override
    public void visitMoveResult(Register register) {
        super.visitMoveResult(register);

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

            visit(new RegisterAccess().withWrite(register, type));
        } else if (predecessor instanceof NewFilledArrayInstruction) {
            visit(new RegisterAccess().withWrite(register, arrayType()));
        } else {
            throw new IllegalStateException("There's nothing to move after a " + predecessor.getClass().getSimpleName());
        }
    }

    @Override
    public void visitMoveException(Register target) {
        super.visitMoveException(target);

        visit(new RegisterAccess().withWrite(target, ObjectType.OBJECT));
    }

    @Override
    public void visitGoto(Instruction target) {
        super.visitGoto(target);

        visit(new RegisterAccess());
    }

    @Override
    public void visitIf(IfInstruction.Comparison comparison, Register op1, Optional<Register> op2Opt, Instruction target) {
        super.visitIf(comparison, op1, op2Opt, target);

        RegisterAccess action = new RegisterAccess();
        action.withRead(op1, IntType.getInstance());
        op2Opt.ifPresent(op2 -> action.withRead(op2, IntType.getInstance()));
        visit(action);
    }

    @Override
    public void visitSwitch(Register value, LinkedHashMap<Integer, Instruction> branchTable) {
        super.visitSwitch(value, branchTable);

        visit(new RegisterAccess().withRead(value, IntType.getInstance()));
    }

    /**
     * Types of the values read and written by instructions into/from registers.
     */
    public static class RegisterAccess {
        private final Map<Register, Optional<Type>> reads;
        private final Map<Register, Optional<Type>> writes;

        public RegisterAccess() {
            this(new HashMap<>(), new HashMap<>());
        }

        public RegisterAccess(Map<Register, Optional<Type>> reads, Map<Register, Optional<Type>> writes) {
            this.reads = reads;
            this.writes = writes;
        }

        private RegisterAccess withRead(Register register, Type type) {
            return withRead(register, Optional.of(type));
        }

        private RegisterAccess withRead(Register register, Optional<Type> type) {
            this.reads.put(register, type);
            return this;
        }

        private RegisterAccess withWrite(Register register, Type type) {
            return withWrite(register, Optional.of(type));
        }

        private RegisterAccess withWrite(Register register, Optional<Type> type) {
            this.writes.put(register, type);
            return this;
        }

        public Map<Register, Optional<Type>> getReads() {
            return reads;
        }

        public Map<Register, Optional<Type>> getWrites() {
            return writes;
        }
    }
}
