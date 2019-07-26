package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.*;
import me.aki.tactical.core.handle.*;
import me.aki.tactical.core.type.*;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.DetailedDexType;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.FillArrayInstruction;
import me.aki.tactical.dex.insn.IfInstruction;
import me.aki.tactical.dex.utils.DexInsnVisitor;
import org.jf.dexlib2.MethodHandleType;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;

import java.util.*;
import java.util.stream.Collectors;

public class SmaliInsnWriter extends DexInsnVisitor<me.aki.tactical.dex.insn.Instruction, Register> {
    // Since dexlib instructions are immutable, we use RWCells and replace their content to mutate instructions.
    private List<RWCell<Instruction>> instructions = new ArrayList<>();

    private Map<me.aki.tactical.dex.insn.Instruction, Set<RWCell<Integer>>> insnRefs = new HashMap<>();

    private void visitInstruction(Instruction insn) {
        visitInstruction(RWCell.of(insn, Instruction.class));
    }

    private void visitInstruction(RWCell<Instruction> insn) {
        instructions.add(insn);
    }

    public List<RWCell<Instruction>> getInstructions() {
        return instructions;
    }

    public List<RWCell<Instruction>> popInstructions() {
        List<RWCell<Instruction>> instructions = this.instructions;
        this.instructions = new ArrayList<>();
        return instructions;
    }

    private Integer convertRegister(Register register) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    private void registerInsnRef(me.aki.tactical.dex.insn.Instruction insn, RWCell<Integer> cell) {
        this.insnRefs.computeIfAbsent(insn, x -> new HashSet<>()).add(cell);
    }

    @Override
    public void visitConstant(DexConstant constant, Register target) {
        if (constant instanceof ClassConstant) {
            RefType classConstant = ((ClassConstant) constant).getValue();
            String type = DexUtils.toDexType(classConstant);
            visitInstruction(new ImmutableInstruction21c(Opcode.CONST_CLASS, convertRegister(target), new ImmutableTypeReference(type)));
        } else if (constant instanceof DexNumberConstant) {
            if (constant instanceof DexNumber32Constant) {
                int literal = ((DexNumber32Constant) constant).intValue();
                if (-8 <= literal && literal <= 7) {
                    visitInstruction(new ImmutableInstruction11n(Opcode.CONST_4, convertRegister(target), literal));
                } else if (Short.MIN_VALUE <= literal && literal <= Short.MAX_VALUE) {
                    visitInstruction(new ImmutableInstruction21s(Opcode.CONST_16, convertRegister(target), literal));
                } else {
                    visitInstruction(new ImmutableInstruction31i(Opcode.CONST, convertRegister(target), literal));
                }
            } else if (constant instanceof DexNumber64Constant) {
                long literal = ((DexNumber64Constant) constant).longValue();
                if (Short.MIN_VALUE <= literal && literal <= Short.MAX_VALUE) {
                    visitInstruction(new ImmutableInstruction21s(Opcode.CONST_WIDE_16, convertRegister(target), (int) literal));
                } else if (Integer.MIN_VALUE <= literal && literal <= Integer.MAX_VALUE) {
                    visitInstruction(new ImmutableInstruction31i(Opcode.CONST_WIDE_32, convertRegister(target), (int) literal));
                } else {
                    visitInstruction(new ImmutableInstruction51l(Opcode.CONST_WIDE, convertRegister(target), literal));
                }
            } else {
                DexUtils.unreachable();
            }
        } else if (constant instanceof HandleConstant) {
            MethodHandleReference reference = convertMethodHandle(((HandleConstant) constant).getHandle());
            visitInstruction(new ImmutableInstruction21c(Opcode.CONST_METHOD_HANDLE, convertRegister(target), reference));
        } else if (constant instanceof MethodTypeConstant) {
            MethodTypeConstant methodType = (MethodTypeConstant) constant;
            String returnType = DexUtils.toDexReturnType(methodType.getReturnType());
            List<String> parameters = methodType.getArgumentTypes().stream()
                    .map(DexUtils::toDexType)
                    .collect(Collectors.toList());

            MethodProtoReference proto = new ImmutableMethodProtoReference(parameters, returnType);
            visitInstruction(new ImmutableInstruction21c(Opcode.CONST_METHOD_TYPE, convertRegister(target), proto));
        } else if (constant instanceof StringConstant) {
            String string = ((StringConstant) constant).getValue();
            visitInstruction(new ImmutableInstruction21c(Opcode.CONST_STRING, convertRegister(target), new ImmutableStringReference(string)));
        } else {
            DexUtils.unreachable();
        }
    }

    private MethodHandleReference convertMethodHandle(Handle handle) {
        int type = match(handle, new HandleMatch<>() {
            public Integer caseGetFieldHandle(GetFieldHandle handle) { return MethodHandleType.INSTANCE_GET; }
            public Integer caseSetFieldHandle(SetFieldHandle handle) { return MethodHandleType.INSTANCE_PUT; }
            public Integer caseGetStaticHandle(GetStaticHandle handle) { return MethodHandleType.STATIC_GET; }
            public Integer caseSetStaticHandle(SetStaticHandle handle) { return MethodHandleType.STATIC_PUT; }

            public Integer caseInvokeStaticHandle(InvokeStaticHandle handle) { return MethodHandleType.INVOKE_STATIC; }
            public Integer caseInvokeInterfaceHandle(InvokeInterfaceHandle handle) { return MethodHandleType.INVOKE_INTERFACE; }
            public Integer caseInvokeSpecialHandle(InvokeSpecialHandle handle) { return MethodHandleType.INVOKE_DIRECT; }
            public Integer caseInvokeVirtualHandle(InvokeVirtualHandle handle) { return MethodHandleType.INVOKE_INSTANCE; }
            public Integer caseNewInstanceHandle(NewInstanceHandle handle) { return MethodHandleType.INVOKE_CONSTRUCTOR; }
        });

        if (handle instanceof FieldHandle) {
            FieldHandle fh = (FieldHandle) handle;
            FieldReference fieldRef = DexUtils.convertFieldRef(fh.getFieldRef());
            return new ImmutableMethodHandleReference(type, fieldRef);
        } else if (handle instanceof MethodHandle) {
            MethodHandle mh = (MethodHandle) handle;
            MethodReference methodRef = DexUtils.convertMethodRef(mh.getMethodRef());
            return new ImmutableMethodHandleReference(type, methodRef);
        } else {
            return DexUtils.unreachable();
        }
    }

    // MATH //

    @Override
    public void visitAdd(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.ADD_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.ADD_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.ADD_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.ADD_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.ADD_INT; }
                public Opcode caseLong() { return Opcode.ADD_LONG; }
                public Opcode caseFloat() { return Opcode.ADD_FLOAT; }
                public Opcode caseDouble() { return Opcode.ADD_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitSub(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SUB_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.SUB_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.SUB_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.SUB_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SUB_INT; }
                public Opcode caseLong() { return Opcode.SUB_LONG; }
                public Opcode caseFloat() { return Opcode.SUB_FLOAT; }
                public Opcode caseDouble() { return Opcode.SUB_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitMul(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.MUL_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.MUL_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.MUL_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.MUL_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.MUL_INT; }
                public Opcode caseLong() { return Opcode.MUL_LONG; }
                public Opcode caseFloat() { return Opcode.MUL_FLOAT; }
                public Opcode caseDouble() { return Opcode.MUL_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitDiv(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.DIV_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.DIV_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.DIV_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.DIV_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.DIV_INT; }
                public Opcode caseLong() { return Opcode.DIV_LONG; }
                public Opcode caseFloat() { return Opcode.DIV_FLOAT; }
                public Opcode caseDouble() { return Opcode.DIV_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitMod(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.REM_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.REM_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.REM_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.REM_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.REM_INT; }
                public Opcode caseLong() { return Opcode.REM_LONG; }
                public Opcode caseFloat() { return Opcode.REM_FLOAT; }
                public Opcode caseDouble() { return Opcode.REM_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitAnd(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.AND_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.AND_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.AND_INT; }
                public Opcode caseLong() { return Opcode.AND_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitOr(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.OR_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.OR_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.OR_INT; }
                public Opcode caseLong() { return Opcode.OR_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitXor(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.XOR_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.XOR_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.XOR_INT; }
                public Opcode caseLong() { return Opcode.XOR_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitShl(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SHL_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.SHL_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SHL_INT; }
                public Opcode caseLong() { return Opcode.SHL_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitShr(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SHR_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.SHR_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SHR_INT; }
                public Opcode caseLong() { return Opcode.SHR_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitUShr(PrimitiveType type, Register op1, Register op2, Register result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.USHR_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.USHR_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(op1), convertRegister(op2)));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.USHR_INT; }
                public Opcode caseLong() { return Opcode.USHR_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
        }
    }

    @Override
    public void visitLitAdd(Register op1, int op2, Register result) {
        super.visitLitAdd(op1, op2, result);
    }

    @Override
    public void visitLitRSub(Register op1, int op2, Register result) {
        super.visitLitRSub(op1, op2, result);
    }

    @Override
    public void visitLitMul(Register op1, int op2, Register result) {
        super.visitLitMul(op1, op2, result);
    }

    @Override
    public void visitLitDiv(Register op1, int op2, Register result) {
        super.visitLitDiv(op1, op2, result);
    }

    @Override
    public void visitLitMod(Register op1, int op2, Register result) {
        super.visitLitMod(op1, op2, result);
    }

    @Override
    public void visitLitAnd(Register op1, int op2, Register result) {
        super.visitLitAnd(op1, op2, result);
    }

    @Override
    public void visitLitOr(Register op1, int op2, Register result) {
        super.visitLitOr(op1, op2, result);
    }

    @Override
    public void visitLitXor(Register op1, int op2, Register result) {
        super.visitLitXor(op1, op2, result);
    }

    @Override
    public void visitLitShl(Register op1, int op2, Register result) {
        super.visitLitShl(op1, op2, result);
    }

    @Override
    public void visitLitShr(Register op1, int op2, Register result) {
        super.visitLitShr(op1, op2, result);
    }

    @Override
    public void visitLitUShr(Register op1, int op2, Register result) {
        super.visitLitUShr(op1, op2, result);
    }

    @Override
    public void visitNeg(PrimitiveType type, Register value, Register result) {
        super.visitNeg(type, value, result);
    }

    @Override
    public void visitNot(PrimitiveType type, Register value, Register result) {
        super.visitNot(type, value, result);
    }

    @Override
    public void visitCmp(Register op1, Register op2, Register result) {
        super.visitCmp(op1, op2, result);
    }

    @Override
    public void visitCmpl(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitCmpl(type, op1, op2, result);
    }

    @Override
    public void visitCmpg(PrimitiveType type, Register op1, Register op2, Register result) {
        super.visitCmpg(type, op1, op2, result);
    }

    @Override
    public void visitArrayLength(Register array, Register result) {
        super.visitArrayLength(array, result);
    }

    @Override
    public void visitArrayLoad(DetailedDexType type, Register array, Register index, Register result) {
        super.visitArrayLoad(type, array, index, result);
    }

    @Override
    public void visitArrayStore(DetailedDexType type, Register array, Register index, Register value) {
        super.visitArrayStore(type, array, index, value);
    }

    @Override
    public void visitFillArray(Register array, List<FillArrayInstruction.NumbericConstant> values) {
        super.visitFillArray(array, values);
    }

    @Override
    public void visitNewArray(ArrayType type, Register size, Register result) {
        super.visitNewArray(type, size, result);
    }

    @Override
    public void visitNewFilledArray(ArrayType type, List<Register> registers) {
        super.visitNewFilledArray(type, registers);
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, Register fromRegister, Register toRegister) {
        super.visitPrimitiveCast(fromType, toType, fromRegister, toRegister);
    }

    @Override
    public void visitRefCast(RefType type, Register register) {
        super.visitRefCast(type, register);
    }

    @Override
    public void visitMonitorEnter(Register value) {
        super.visitMonitorEnter(value);
    }

    @Override
    public void visitMonitorExit(Register value) {
        super.visitMonitorExit(value);
    }

    @Override
    public void visitNew(Path type, Register result) {
        super.visitNew(type, result);
    }

    @Override
    public void visitInstanceOf(RefType type, Register value, Register result) {
        super.visitInstanceOf(type, value, result);
    }

    @Override
    public void visitReturn(DexType type, Register register) {
        super.visitReturn(type, register);
    }

    @Override
    public void visitReturnVoid() {
        super.visitReturnVoid();
    }

    @Override
    public void visitThrow(Register exception) {
        super.visitThrow(exception);
    }

    @Override
    public void visitFieldGet(FieldRef field, Optional<Register> instance, Register result) {
        super.visitFieldGet(field, instance, result);
    }

    @Override
    public void visitFieldSet(FieldRef field, Optional<Register> instance, Register value) {
        super.visitFieldSet(field, instance, value);
    }

    @Override
    public void visitInvoke(InvokeType invoke, MethodRef method, Optional<Register> instance, List<Register> arguments) {
        super.visitInvoke(invoke, method, instance, arguments);
    }

    @Override
    public void visitCustomInvoke(List<Register> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
        super.visitCustomInvoke(arguments, name, descriptor, bootstrapArguments, bootstrapMethod);
    }

    @Override
    public void visitMove(DexType type, Register from, Register to) {
        super.visitMove(type, from, to);
    }

    @Override
    public void visitMoveResult(DexType type, Register register) {
        super.visitMoveResult(type, register);
    }

    @Override
    public void visitMoveException(Register target) {
        super.visitMoveException(target);
    }

    // Branch instructions

    @Override
    public void visitGoto(me.aki.tactical.dex.insn.Instruction target) {
        GotoInsnCell insnCell = new GotoInsnCell();
        registerInsnRef(target, insnCell.getOffsetCell());
        visitInstruction(insnCell);
    }

    class GotoInsnCell extends NonInitializedCell<Instruction> {
        private RWCell<Integer> offsetCell = new InnerNonInitializedCell<>(Integer.class, this);

        public GotoInsnCell() {
            super(Instruction.class);
        }

        public RWCell<Integer> getOffsetCell() {
            return offsetCell;
        }

        @Override
        protected Instruction uninitializedValue() {
            int offset = offsetCell.get();

            if (-128 <= offset && offset <= 127) {
                return new ImmutableInstruction10t(Opcode.GOTO, offset);
            } else if (-32768 <= offset && offset <= 32767) {
                return new ImmutableInstruction20t(Opcode.GOTO_16, offset);
            } else {
                return new ImmutableInstruction30t(Opcode.GOTO_32, offset);
            }
        }
    }

    @Override
    public void visitIf(IfInstruction.Comparison comparison, Register op1, Optional<Register> op2Opt, me.aki.tactical.dex.insn.Instruction target) {
        if (op2Opt.isPresent()) {
            Register op2 = op2Opt.get();
            Opcode opcode = getTwoRegisterComparionsOpcode(comparison);

            Insn22tCell insnCell = new Insn22tCell(opcode, convertRegister(op1), convertRegister(op2));
            registerInsnRef(target, insnCell.getOffsetCell());
            visitInstruction(insnCell);
        } else {
            Opcode opcode = getZeroComparionsOpcode(comparison);

            Insn21tCell insnCell = new Insn21tCell(opcode, convertRegister(op1));
            registerInsnRef(target, insnCell.getOffsetCell());
            visitInstruction(insnCell);
        }
    }

    private Opcode getTwoRegisterComparionsOpcode(IfInstruction.Comparison comparison) {
        switch (comparison) {
            case EQUAL: return Opcode.IF_EQ;
            case NON_EQUAL: return Opcode.IF_NE;
            case LESS_THAN: return Opcode.IF_LT;
            case LESS_EQUAL: return Opcode.IF_LE;
            case GREATER_THAN: return Opcode.IF_GT;
            case GREATER_EQUAL: return Opcode.IF_GE;
            default: return DexUtils.unreachable();
        }
    }

    private Opcode getZeroComparionsOpcode(IfInstruction.Comparison comparison) {
        switch (comparison) {
            case EQUAL: return Opcode.IF_EQZ;
            case NON_EQUAL: return Opcode.IF_NEZ;
            case LESS_THAN: return Opcode.IF_LTZ;
            case LESS_EQUAL: return Opcode.IF_LEZ;
            case GREATER_THAN: return Opcode.IF_GTZ;
            case GREATER_EQUAL: return Opcode.IF_GEZ;
            default: return DexUtils.unreachable();
        }
    }

    @Override
    public void visitSwitch(Register value, LinkedHashMap<Integer, me.aki.tactical.dex.insn.Instruction> branchTable) {
        super.visitSwitch(value, branchTable);
    }

    class Insn21tCell extends NonInitializedCell<Instruction> {
        private final Opcode opcode;
        private final int registerA;
        private final RWCell<Integer> offsetCell = new InnerNonInitializedCell<>(Integer.class, this);

        public Insn21tCell(Opcode opcode, int registerA) {
            super(Instruction.class);
            this.opcode = opcode;
            this.registerA = registerA;
        }

        public RWCell<Integer> getOffsetCell() {
            return offsetCell;
        }

        @Override
        protected Instruction uninitializedValue() {
            Integer offset = offsetCell.get();
            return new ImmutableInstruction21t(opcode, registerA, offset);
        }
    }

    class Insn22tCell extends NonInitializedCell<Instruction> {
        private final Opcode opcode;
        private final int registerA;
        private final int registerB;
        private final RWCell<Integer> offsetCell = new InnerNonInitializedCell<>(Integer.class, this);

        public Insn22tCell(Opcode opcode, int registerA, int registerB) {
            super(Instruction.class);
            this.opcode = opcode;
            this.registerA = registerA;
            this.registerB = registerB;
        }

        public RWCell<Integer> getOffsetCell() {
            return offsetCell;
        }

        @Override
        protected Instruction uninitializedValue() {
            int offset = offsetCell.get();
            return new ImmutableInstruction22t(opcode, registerA, registerB, offset);
        }
    }

    /**
     * A {@link NonInitializedCell} that denies write operation if another 'outer' {@link NonInitializedCell} has already been initialized.
     *
     * This cell is used in cases where {@link NonInitializedCell#uninitializedValue() the computation of the default value}
     * of a {@link NonInitializedCell} uses the content of this cell. If that cell gets initialized by calling the
     * {@link NonInitializedCell#set(Object)} value, altering this cell will have no effects.
     * This is unwanted behavior and therefore causes an exception.
     */
    class InnerNonInitializedCell<T> extends NonInitializedCell<T> {
        private final NonInitializedCell<?> outerCell;

        public InnerNonInitializedCell(Class<T> type, NonInitializedCell<?> outerCell) {
            super(type);
            this.outerCell = outerCell;
        }

        @Override
        public void set(T newValue) {
            if (outerCell.isInitialized) {
                throw new RuntimeException("Outer cell should not be initialized");
            }

            super.set(newValue);
        }
    }

    /**
     * A cell that has no value by default.
     * It has a method {@link NonInitializedCell#uninitializedValue() for computation of a default value} that can be overridden.
     */
    class NonInitializedCell<T> extends RWCell<T> {
        private boolean isInitialized = false;
        private T value;

        public NonInitializedCell(Class<T> type) {
            super(type);
        }

        @Override
        public T get() {
            return this.isInitialized ? this.value : uninitializedValue();
        }

        /**
         * Check whether this cell was initialed by calling the set method.
         *
         * @return was the set method of this cell called at least once
         */
        public boolean isInitialized() {
            return this.isInitialized;
        }

        /**
         * Compute a fallback value or throw an exception if no value was set yet.
         */
        protected T uninitializedValue() {
            throw new RuntimeException("Cell was not yet initialized");
        }

        @Override
        public void set(T newValue) {
            this.value = newValue;
            this.isInitialized = true;
        }
    }

    // UTILS //

    private <T> T match(RefType type, RATypeMatch<T> matcher) {
        if (type instanceof ObjectType) {
            return matcher.caseObject((ObjectType) type);
        } else if (type instanceof ArrayType) {
            return matcher.caseArray((ArrayType) type);
        } else {
            return DexUtils.unreachable();
        }
    }

    private <T> T match(PrimitiveType type, ILFDTypeMatch<T> matcher) {
        if (type instanceof IntLikeType) {
            return matcher.caseIntLike((IntLikeType) type);
        } else if (type instanceof LongType) {
            return matcher.caseLong();
        } else if (type instanceof FloatType) {
            return matcher.caseFloat();
        } else if (type instanceof DoubleType) {
            return matcher.caseDouble();
        } else {
            return DexUtils.unreachable();
        }
    }

    private <T> T match(PrimitiveType type, ILTypeMatch<T> matcher) {
        if (type instanceof IntLikeType) {
            return matcher.caseIntLike((IntLikeType) type);
        } else if (type instanceof LongType) {
            return matcher.caseLong();
        } else {
            return DexUtils.unreachable();
        }
    }

    private <T> T match(Handle handle, HandleMatch<T> matcher) {
        if (handle instanceof FieldHandle) {
            if (handle instanceof GetFieldHandle) {
                return matcher.caseGetFieldHandle((GetFieldHandle) handle);
            } else if (handle instanceof SetFieldHandle) {
                return matcher.caseSetFieldHandle((SetFieldHandle) handle);
            } else if (handle instanceof GetStaticHandle) {
                return matcher.caseGetStaticHandle((GetStaticHandle) handle);
            } else if (handle instanceof SetStaticHandle) {
                return matcher.caseSetStaticHandle((SetStaticHandle) handle);
            } else {
                return DexUtils.unreachable();
            }
        } else if(handle instanceof MethodHandle) {
            if (handle instanceof InvokeStaticHandle) {
                return matcher.caseInvokeStaticHandle((InvokeStaticHandle) handle);
            } else if (handle instanceof InvokeInterfaceHandle) {
                return matcher.caseInvokeInterfaceHandle((InvokeInterfaceHandle) handle);
            } else if (handle instanceof InvokeSpecialHandle) {
                return matcher.caseInvokeSpecialHandle((InvokeSpecialHandle) handle);
            } else if (handle instanceof InvokeVirtualHandle) {
                return matcher.caseInvokeVirtualHandle((InvokeVirtualHandle) handle);
            } else if (handle instanceof NewInstanceHandle) {
                return matcher.caseNewInstanceHandle((NewInstanceHandle) handle);
            } else {
                return DexUtils.unreachable();
            }
        } else {
            return DexUtils.unreachable();
        }
    }

    interface RATypeMatch<T> {
        T caseArray(ArrayType type);
        T caseObject(ObjectType type);
    }

    interface ILTypeMatch<T> {
        T caseIntLike(IntLikeType type);
        T caseLong();
    }

    interface ILFDTypeMatch<T> {
        T caseIntLike(IntLikeType type);
        T caseLong();
        T caseFloat();
        T caseDouble();
    }

    interface HandleMatch<T> {
        T caseGetFieldHandle(GetFieldHandle handle);
        T caseSetFieldHandle(SetFieldHandle handle);
        T caseGetStaticHandle(GetStaticHandle handle);
        T caseSetStaticHandle(SetStaticHandle handle);

        T caseInvokeStaticHandle(InvokeStaticHandle handle);
        T caseInvokeInterfaceHandle(InvokeInterfaceHandle handle);
        T caseInvokeSpecialHandle(InvokeSpecialHandle handle);
        T caseInvokeVirtualHandle(InvokeVirtualHandle handle);
        T caseNewInstanceHandle(NewInstanceHandle handle);
    }
}
