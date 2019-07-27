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
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import org.jf.dexlib2.immutable.value.*;

import java.util.*;
import java.util.stream.Collectors;

public class SmaliInsnWriter extends DexInsnVisitor<me.aki.tactical.dex.insn.Instruction, Register> {
    // Since dexlib instructions are immutable, we use RWCells and replace their content to mutate instructions.
    private List<RWCell<Instruction>> instructions = new ArrayList<>();

    private List<RWCell<Instruction>> payloadInstructions = new ArrayList<>();

    private Map<me.aki.tactical.dex.insn.Instruction, Set<RWCell<Integer>>> insnRefs = new HashMap<>();
    private List<OffsetInsnRef> offsetInsnRefs = new ArrayList<>();

    private int callSiteIndex = 0;

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

    public List<RWCell<Instruction>> getPayloadInstructions() {
        return payloadInstructions;
    }

    public List<RWCell<Instruction>> popPayloadInstructions() {
        List<RWCell<Instruction>> instructions = this.payloadInstructions;
        this.payloadInstructions = new ArrayList<>();
        return instructions;
    }

    private Integer convertRegister(Register register) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    private void registerInsnRef(me.aki.tactical.dex.insn.Instruction insn, RWCell<Integer> cell) {
        this.insnRefs.computeIfAbsent(insn, x -> new HashSet<>()).add(cell);
    }

    private void registerOffsetInsnRef(RWCell<Instruction> relativeTo, RWCell<Instruction> target, RWCell<Integer> offsetCell) {
        this.offsetInsnRefs.add(new OffsetInsnRef(relativeTo, target, offsetCell));
    }

    @Override
    public void visitConstant(DexConstant constant, Register target) {
        if (constant instanceof ClassConstant) {
            RefType classConstant = ((ClassConstant) constant).getValue();
            String type = DexUtils.toDexType(classConstant);
            visitInstruction(new ImmutableInstruction21c(Opcode.CONST_CLASS, convertRegister(target), new ImmutableTypeReference(type)));
        } else if (constant instanceof DexNumberConstant) {
            visitNumberConstant((DexNumberConstant) constant, target);
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

    private void visitNumberConstant(DexNumberConstant constant, Register target) {
        if (constant instanceof DexNumber32Constant) {
            int literal = ((DexNumber32Constant) constant).intValue();
            if (-8 <= literal && literal <= 7) {
                visitInstruction(new ImmutableInstruction11n(Opcode.CONST_4, convertRegister(target), literal));
            } else if (Short.MIN_VALUE <= literal && literal <= Short.MAX_VALUE) {
                visitInstruction(new ImmutableInstruction21s(Opcode.CONST_16, convertRegister(target), literal));
            } else if ((literal & 0x0000FFFF) == 0) {
                visitInstruction(new ImmutableInstruction21ih(Opcode.CONST_WIDE_HIGH16, convertRegister(target), literal));
            } else {
                visitInstruction(new ImmutableInstruction31i(Opcode.CONST, convertRegister(target), literal));
            }
        } else if (constant instanceof DexNumber64Constant) {
            long literal = ((DexNumber64Constant) constant).longValue();
            if (Short.MIN_VALUE <= literal && literal <= Short.MAX_VALUE) {
                visitInstruction(new ImmutableInstruction21s(Opcode.CONST_WIDE_16, convertRegister(target), (int) literal));
            } else if (Integer.MIN_VALUE <= literal && literal <= Integer.MAX_VALUE) {
                visitInstruction(new ImmutableInstruction31i(Opcode.CONST_WIDE_32, convertRegister(target), (int) literal));
            } else if ((literal & 0x0000FFFFFFFFFFFL) == 0) {
                visitInstruction(new ImmutableInstruction21lh(Opcode.CONST_WIDE_HIGH16, convertRegister(target), literal));
            } else {
                visitInstruction(new ImmutableInstruction51l(Opcode.CONST_WIDE, convertRegister(target), literal));
            }
        } else {
            DexUtils.unreachable();
        }
    }

    private ImmutableMethodHandleReference convertMethodHandle(Handle handle) {
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

    // LITERAL MATH //

    @Override
    public void visitLitAdd(Register op1, short literal, Register result) {
        visitLiteralMathInsn(Opcode.ADD_INT_LIT8, Opcode.ADD_INT_LIT16, op1, literal, result);
    }

    @Override
    public void visitLitRSub(Register op1, short literal, Register result) {
        visitLiteralMathInsn(Opcode.RSUB_INT_LIT8, Opcode.RSUB_INT, op1, literal, result);
    }

    @Override
    public void visitLitMul(Register op1, short literal, Register result) {
        visitLiteralMathInsn(Opcode.MUL_INT_LIT8, Opcode.MUL_INT_LIT16, op1, literal, result);
    }

    @Override
    public void visitLitDiv(Register op1, short literal, Register result) {
        visitLiteralMathInsn(Opcode.DIV_INT_LIT8, Opcode.DIV_INT_LIT16, op1, literal, result);
    }

    @Override
    public void visitLitMod(Register op1, short literal, Register result) {
        visitLiteralMathInsn(Opcode.REM_INT_LIT8, Opcode.REM_INT_LIT16, op1, literal, result);
    }

    @Override
    public void visitLitAnd(Register op1, short literal, Register result) {
        visitLiteralMathInsn(Opcode.AND_INT_LIT8, Opcode.AND_INT_LIT16, op1, literal, result);
    }

    @Override
    public void visitLitOr(Register op1, short literal, Register result) {
        visitLiteralMathInsn(Opcode.OR_INT_LIT8, Opcode.OR_INT_LIT16, op1, literal, result);
    }

    @Override
    public void visitLitXor(Register op1, short literal, Register result) {
        visitLiteralMathInsn(Opcode.XOR_INT_LIT8, Opcode.XOR_INT_LIT16, op1, literal, result);
    }

    private void visitLiteralMathInsn(Opcode lit8, Opcode lit16, Register op1, short literal, Register result) {
        if (Byte.MIN_VALUE <= literal && literal <= Byte.MAX_VALUE) {
            visitInstruction(new ImmutableInstruction22b(lit8, convertRegister(result), convertRegister(op1), literal));
        } else {
            visitInstruction(new ImmutableInstruction22s(lit16, convertRegister(result), convertRegister(op1), literal));
        }
    }

    @Override
    public void visitLitShl(Register op1, short literal, Register result) {
        visitInstruction(new ImmutableInstruction22b(Opcode.SHL_INT_LIT8, convertRegister(result), convertRegister(op1), literal));
    }

    @Override
    public void visitLitShr(Register op1, short literal, Register result) {
        visitInstruction(new ImmutableInstruction22b(Opcode.SHR_INT_LIT8, convertRegister(result), convertRegister(op1), literal));
    }

    @Override
    public void visitLitUShr(Register op1, short literal, Register result) {
        visitInstruction(new ImmutableInstruction22b(Opcode.USHR_INT_LIT8, convertRegister(result), convertRegister(op1), literal));
    }

    @Override
    public void visitNeg(PrimitiveType type, Register value, Register result) {
        Opcode opcode = match(type, new ILFDTypeMatch<>() {
            public Opcode caseIntLike(IntLikeType type) { return Opcode.NEG_INT; }
            public Opcode caseLong() { return Opcode.NEG_LONG; }
            public Opcode caseFloat() { return Opcode.NEG_FLOAT; }
            public Opcode caseDouble() { return Opcode.NEG_DOUBLE; }
        });

        visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(result), convertRegister(value)));
    }

    @Override
    public void visitNot(PrimitiveType type, Register value, Register result) {
        Opcode opcode = match(type, new ILTypeMatch<>() {
            public Opcode caseIntLike(IntLikeType type) { return Opcode.NOT_INT; }
            public Opcode caseLong() { return Opcode.NOT_LONG; }
        });

        visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(result), convertRegister(value)));
    }

    @Override
    public void visitCmp(Register op1, Register op2, Register result) {
        visitInstruction(new ImmutableInstruction23x(Opcode.CMP_LONG, convertRegister(result), convertRegister(op1), convertRegister(op2)));
    }

    @Override
    public void visitCmpl(PrimitiveType type, Register op1, Register op2, Register result) {
        Opcode opcode = match(type, new FDTypeMatch<>() {
            public Opcode caseFloat() { return Opcode.CMPL_FLOAT; }
            public Opcode caseDouble() { return Opcode.CMPL_DOUBLE; }
        });

        visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
    }

    @Override
    public void visitCmpg(PrimitiveType type, Register op1, Register op2, Register result) {
        Opcode opcode = match(type, new FDTypeMatch<>() {
            public Opcode caseFloat() { return Opcode.CMPG_FLOAT; }
            public Opcode caseDouble() { return Opcode.CMPG_DOUBLE; }
        });

        visitInstruction(new ImmutableInstruction23x(opcode, convertRegister(result), convertRegister(op1), convertRegister(op2)));
    }

    // ARRAY INSTRUCTIONS //

    @Override
    public void visitArrayLength(Register array, Register result) {
        visitInstruction(new ImmutableInstruction12x(Opcode.ARRAY_LENGTH, convertRegister(result), convertRegister(array)));
    }

    @Override
    public void visitArrayLoad(DetailedDexType type, Register array, Register index, Register result) {
        visitInstruction(new ImmutableInstruction23x(getArrayLoadOpcode(type), convertRegister(result), convertRegister(array), convertRegister(index)));
    }

    private Opcode getArrayLoadOpcode(DetailedDexType type) {
        switch (type) {
            case BOOLEAN: return Opcode.AGET_BOOLEAN;
            case BYTE: return Opcode.AGET_BYTE;
            case SHORT: return Opcode.AGET_SHORT;
            case CHAR: return Opcode.AGET_CHAR;
            case NORMAL: return Opcode.AGET;
            case OBJECT: return Opcode.AGET_OBJECT;
            case WIDE: return Opcode.AGET_WIDE;
            default: return DexUtils.unreachable();
        }
    }

    @Override
    public void visitArrayStore(DetailedDexType type, Register array, Register index, Register value) {
        visitInstruction(new ImmutableInstruction23x(getArrayStoreOpcode(type), convertRegister(value), convertRegister(array), convertRegister(index)));
    }

    private Opcode getArrayStoreOpcode(DetailedDexType type) {
        switch (type) {
            case BOOLEAN: return Opcode.APUT_BOOLEAN;
            case BYTE: return Opcode.APUT_BYTE;
            case SHORT: return Opcode.APUT_SHORT;
            case CHAR: return Opcode.APUT_CHAR;
            case NORMAL: return Opcode.APUT;
            case OBJECT: return Opcode.APUT_OBJECT;
            case WIDE: return Opcode.APUT_WIDE;
            default: return DexUtils.unreachable();
        }
    }

    @Override
    public void visitFillArray(Register array, FillArrayInstruction.NumberSize elementSize, List<FillArrayInstruction.NumericConstant> values) {
        Insn31tCell insnCell = new Insn31tCell(Opcode.FILL_ARRAY_DATA, convertRegister(array));

        List<Number> numbers = values.stream().map(FillArrayInstruction.NumericConstant::longValue).collect(Collectors.toList());
        RWCell<Instruction> arrayPayload = RWCell.of(new ImmutableArrayPayload(elementSize.getByteSize(), numbers), Instruction.class);

        registerOffsetInsnRef(insnCell, arrayPayload, insnCell.getOffsetCell());
        this.payloadInstructions.add(arrayPayload);
    }

    @Override
    public void visitNewArray(ArrayType type, Register size, Register result) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toDexType(type));
        visitInstruction(new ImmutableInstruction22c(Opcode.NEW_ARRAY, convertRegister(result), convertRegister(size), typeRef));
    }

    @Override
    public void visitNewFilledArray(ArrayType type, List<Register> registers) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toDexType(type));

        int registerCount = registers.size();
        if (registerCount <= 5) {
            Iterator<Integer> registerIter = registers.stream().map(this::convertRegister).iterator();
            int registerC = registerIter.hasNext() ? registerIter.next() : 0;
            int registerD = registerIter.hasNext() ? registerIter.next() : 0;
            int registerE = registerIter.hasNext() ? registerIter.next() : 0;
            int registerF = registerIter.hasNext() ? registerIter.next() : 0;
            int registerG = registerIter.hasNext() ? registerIter.next() : 0;
            visitInstruction(new ImmutableInstruction35c(Opcode.FILLED_NEW_ARRAY, registerCount, registerC, registerD, registerE, registerF, registerG, typeRef));
        } else {
            //TODO The registers musts have indices that follow each other
            throw new RuntimeException("Not Yet implemented");
        }
    }

    // CASTS //

    @Override
    public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, Register fromRegister, Register toRegister) {
        Opcode opcode = match(fromType, new ILFDTypeMatch<>() {
            public Opcode caseIntLike(IntLikeType type) {
                return match(toType, new PrimitiveTypeMatch<>() {
                    public Opcode caseBoolean() { return DexUtils.unreachable(); }
                    public Opcode caseByte() { return Opcode.INT_TO_BYTE; }
                    public Opcode caseChar() { return Opcode.INT_TO_CHAR; }
                    public Opcode caseShort() { return Opcode.INT_TO_SHORT; }
                    public Opcode caseInt() { return DexUtils.unreachable(); }
                    public Opcode caseLong() { return Opcode.INT_TO_LONG; }
                    public Opcode caseFloat() { return Opcode.INT_TO_FLOAT; }
                    public Opcode caseDouble() { return Opcode.INT_TO_DOUBLE; }
                });
            }

            public Opcode caseLong() {
                return match(toType, new ILFDTypeMatch<>() {
                    public Opcode caseIntLike(IntLikeType type) { return Opcode.LONG_TO_INT; }
                    public Opcode caseLong() { return DexUtils.unreachable();}
                    public Opcode caseFloat() { return Opcode.LONG_TO_FLOAT; }
                    public Opcode caseDouble() { return Opcode.LONG_TO_DOUBLE; }
                });
            }

            public Opcode caseFloat() {
                return match(toType, new ILFDTypeMatch<>() {
                    public Opcode caseIntLike(IntLikeType type) { return Opcode.FLOAT_TO_INT; }
                    public Opcode caseLong() { return Opcode.FLOAT_TO_LONG; }
                    public Opcode caseFloat() { return DexUtils.unreachable(); }
                    public Opcode caseDouble() { return Opcode.FLOAT_TO_DOUBLE; }
                });
            }

            public Opcode caseDouble() {
                return match(toType, new ILFDTypeMatch<>() {
                    public Opcode caseIntLike(IntLikeType type) { return Opcode.DOUBLE_TO_INT; }
                    public Opcode caseLong() { return Opcode.DOUBLE_TO_LONG; }
                    public Opcode caseFloat() { return Opcode.DOUBLE_TO_FLOAT; }
                    public Opcode caseDouble() { return DexUtils.unreachable(); }
                });
            }
        });

        visitInstruction(new ImmutableInstruction12x(opcode, convertRegister(toRegister), convertRegister(fromRegister)));
    }

    @Override
    public void visitRefCast(RefType type, Register register) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toDexType(type));
        visitInstruction(new ImmutableInstruction21c(Opcode.CHECK_CAST, convertRegister(register), typeRef));
    }

    // MONITOR INSTRUCTIONS //

    @Override
    public void visitMonitorEnter(Register value) {
        visitInstruction(new ImmutableInstruction11x(Opcode.MONITOR_ENTER, convertRegister(value)));
    }

    @Override
    public void visitMonitorExit(Register value) {
        visitInstruction(new ImmutableInstruction11x(Opcode.MONITOR_EXIT, convertRegister(value)));
    }

    // INSTANCE OPERATIONS //

    @Override
    public void visitNew(Path type, Register result) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toObjectDescriptor(type));
        visitInstruction(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, convertRegister(result), typeRef));
    }

    @Override
    public void visitInstanceOf(RefType type, Register value, Register result) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toDexType(type));
        visitInstruction(new ImmutableInstruction22c(Opcode.INSTANCE_OF, convertRegister(result), convertRegister(value), typeRef));
    }

    // METHOD EXIT INSTRUCTIONS //

    @Override
    public void visitReturn(DexType type, Register register) {
        visitInstruction(new ImmutableInstruction11x(getReturnOpcode(type), convertRegister(register)));
    }

    private Opcode getReturnOpcode(DexType type) {
        switch (type) {
            case NORMAL: return Opcode.RETURN;
            case WIDE: return Opcode.RETURN_WIDE;
            case OBJECT: return Opcode.RETURN_OBJECT;
            default: return DexUtils.unreachable();
        }
    }

    @Override
    public void visitReturnVoid() {
        visitInstruction(new ImmutableInstruction10x(Opcode.RETURN_VOID));
    }

    @Override
    public void visitThrow(Register exception) {
        visitInstruction(new ImmutableInstruction11x(Opcode.THROW, convertRegister(exception)));
    }

    // FIELD ACCESS //

    @Override
    public void visitFieldGet(FieldRef field, Optional<Register> instance, Register result) {
        FieldReference fieldRef = DexUtils.convertFieldRef(field);
        boolean isInstance = instance.isPresent();

        Opcode opcode = match(field.getType(), new TypeMatch<>() {
            public Opcode caseBoolean() { return isInstance ? Opcode.IGET_BOOLEAN : Opcode.SGET_BOOLEAN; }
            public Opcode caseByte() { return isInstance ? Opcode.IGET_BYTE : Opcode.SGET_BYTE; }
            public Opcode caseChar() { return isInstance ? Opcode.IGET_CHAR : Opcode.SGET_CHAR; }
            public Opcode caseShort() { return isInstance ? Opcode.IGET_SHORT : Opcode.SGET_SHORT; }
            public Opcode caseInt() { return isInstance ? Opcode.IGET : Opcode.SGET; }
            public Opcode caseLong() { return isInstance ? Opcode.IGET_WIDE : Opcode.SGET_WIDE; }
            public Opcode caseFloat() { return isInstance ? Opcode.IGET : Opcode.SGET; }
            public Opcode caseDouble() { return isInstance ? Opcode.IGET_WIDE : Opcode.SGET_WIDE; }

            public Opcode caseArray(ArrayType type) { return isInstance ? Opcode.IGET_OBJECT : Opcode.SGET_OBJECT; }
            public Opcode caseObject(ObjectType type) { return isInstance ? Opcode.IGET_OBJECT : Opcode.SGET_OBJECT; }
        });

        if (isInstance) {
            visitInstruction(new ImmutableInstruction22c(opcode, convertRegister(result), convertRegister(instance.get()), fieldRef));
        } else {
            visitInstruction(new ImmutableInstruction21c(opcode, convertRegister(result), fieldRef));
        }
    }

    @Override
    public void visitFieldSet(FieldRef field, Optional<Register> instance, Register value) {
        FieldReference fieldRef = DexUtils.convertFieldRef(field);
        boolean isInstance = instance.isPresent();

        Opcode opcode = match(field.getType(), new TypeMatch<>() {
            public Opcode caseBoolean() { return isInstance ? Opcode.IPUT_BOOLEAN : Opcode.SPUT_BOOLEAN; }
            public Opcode caseByte() { return isInstance ? Opcode.IPUT_BYTE : Opcode.SPUT_BYTE; }
            public Opcode caseChar() { return isInstance ? Opcode.IPUT_CHAR : Opcode.SPUT_CHAR; }
            public Opcode caseShort() { return isInstance ? Opcode.IPUT_SHORT : Opcode.SPUT_SHORT; }
            public Opcode caseInt() { return isInstance ? Opcode.IPUT : Opcode.SPUT; }
            public Opcode caseLong() { return isInstance ? Opcode.IPUT_WIDE : Opcode.SPUT_WIDE; }
            public Opcode caseFloat() { return isInstance ? Opcode.IPUT : Opcode.SPUT; }
            public Opcode caseDouble() { return isInstance ? Opcode.IPUT_WIDE : Opcode.SPUT_WIDE; }

            public Opcode caseArray(ArrayType type) { return isInstance ? Opcode.IPUT_OBJECT : Opcode.SPUT_OBJECT; }
            public Opcode caseObject(ObjectType type) { return isInstance ? Opcode.IPUT_OBJECT : Opcode.SPUT_OBJECT; }
        });

        if (isInstance) {
            visitInstruction(new ImmutableInstruction22c(opcode, convertRegister(value), convertRegister(instance.get()), fieldRef));
        } else {
            visitInstruction(new ImmutableInstruction21c(opcode, convertRegister(value), fieldRef));
        }
    }

    @Override
    public void visitInvoke(InvokeType invoke, MethodRef method, Optional<Register> instance, List<Register> arguments) {
        MethodReference methodRef = DexUtils.convertMethodRef(method);

        List<Register> registers;
        if (instance.isPresent()) {
            registers = new ArrayList<>(arguments.size() + 1);
            registers.add(instance.get());
            registers.addAll(arguments);
        } else {
            registers = new ArrayList<>(arguments);
        }

        int registerCount = registers.size();
        if (registerCount < 5) {
            Opcode opcode = getInvokeOpcode(invoke);
            Iterator<Integer> registerIter = registers.stream().map(this::convertRegister).iterator();
            int registerC = registerIter.hasNext() ? registerIter.next() : 0;
            int registerD = registerIter.hasNext() ? registerIter.next() : 0;
            int registerE = registerIter.hasNext() ? registerIter.next() : 0;
            int registerF = registerIter.hasNext() ? registerIter.next() : 0;
            int registerG = registerIter.hasNext() ? registerIter.next() : 0;
            visitInstruction(new ImmutableInstruction35c(opcode, registerCount, registerC, registerD, registerE, registerF, registerG, methodRef));
        } else {
            Opcode opcode = getInvokeRangeOpcode(invoke);
            //TODO The registers musts have indices that follow each other
            throw new RuntimeException("Not Yet implemented");
        }
    }

    private Opcode getInvokeOpcode(InvokeType invoke) {
        switch (invoke) {
            case DIRECT: return Opcode.INVOKE_DIRECT;
            case INTERFACE: return Opcode.INVOKE_INTERFACE;
            case POLYMORPHIC: return Opcode.INVOKE_POLYMORPHIC;
            case STATIC: return Opcode.INVOKE_STATIC;
            case SUPER: return Opcode.INVOKE_SUPER;
            case VIRTUAL: return Opcode.INVOKE_VIRTUAL;
            default: return DexUtils.unreachable();
        }
    }

    private Opcode getInvokeRangeOpcode(InvokeType invoke) {
        switch (invoke) {
            case DIRECT: return Opcode.INVOKE_DIRECT_RANGE;
            case INTERFACE: return Opcode.INVOKE_INTERFACE_RANGE;
            case POLYMORPHIC: return Opcode.INVOKE_POLYMORPHIC_RANGE;
            case STATIC: return Opcode.INVOKE_STATIC_RANGE;
            case SUPER: return Opcode.INVOKE_SUPER_RANGE;
            case VIRTUAL: return Opcode.INVOKE_VIRTUAL_RANGE;
            default: return DexUtils.unreachable();
        }
    }


    @Override
    public void visitCustomInvoke(List<Register> arguments, String methodName, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {

        List<EncodedValue> extraArguments = bootstrapArguments.stream()
                .map(this::convertBootstrapConstant)
                .collect(Collectors.toList());

        String returnType = DexUtils.toDexReturnType(descriptor.getReturnType());
        List<String> parameters = descriptor.getParameterTypes().stream()
                .map(DexUtils::toDexType)
                .collect(Collectors.toList());
        MethodProtoReference methodProto = new ImmutableMethodProtoReference(parameters, returnType);

        String callSiteName = Integer.toString(this.callSiteIndex++);
        CallSiteReference callSiteRef = new ImmutableCallSiteReference(callSiteName, convertMethodHandle(bootstrapMethod), methodName, methodProto, extraArguments);

        int registerCount = arguments.size();
        if (registerCount < 5) {
            Iterator<Integer> registerIter = arguments.stream().map(this::convertRegister).iterator();
            int registerC = registerIter.hasNext() ? registerIter.next() : 0;
            int registerD = registerIter.hasNext() ? registerIter.next() : 0;
            int registerE = registerIter.hasNext() ? registerIter.next() : 0;
            int registerF = registerIter.hasNext() ? registerIter.next() : 0;
            int registerG = registerIter.hasNext() ? registerIter.next() : 0;
            visitInstruction(new ImmutableInstruction35c(Opcode.INVOKE_CUSTOM, registerCount, registerC, registerD, registerE, registerF, registerG, callSiteRef));
        } else {
            //TODO The registers musts have indices that follow each other
            throw new RuntimeException("Not Yet implemented");
        }
    }

    private EncodedValue convertBootstrapConstant(BootstrapConstant bootstrapConstant) {
        return match(bootstrapConstant, new BootstrapConstantMatch<>() {
            public EncodedValue caseIntConstant(IntConstant constant) { return new ImmutableIntEncodedValue(constant.getValue()); }
            public EncodedValue caseLongConstant(LongConstant constant) { return new ImmutableLongEncodedValue(constant.getValue()); }
            public EncodedValue caseFloatConstant(FloatConstant constant) { return new ImmutableFloatEncodedValue(constant.getValue()); }
            public EncodedValue caseDoubleConstant(DoubleConstant constant) { return new ImmutableDoubleEncodedValue(constant.getValue()); }
            public EncodedValue caseStringConstant(StringConstant constant) { return new ImmutableStringEncodedValue(constant.getValue()); }

            public EncodedValue caseClassConstant(ClassConstant constant) {
                String typeDesc = DexUtils.toDexType(constant.getValue());
                return new ImmutableTypeEncodedValue(typeDesc);
            }

            public EncodedValue caseHandleConstant(HandleConstant constant) {
                return new ImmutableMethodHandleEncodedValue(convertMethodHandle(constant.getHandle()));
            }

            public EncodedValue caseMethodTypeConstant(MethodTypeConstant constant) {
                String returnType = DexUtils.toDexReturnType(constant.getReturnType());
                List<String> parameters = constant.getArgumentTypes().stream()
                        .map(DexUtils::toDexType)
                        .collect(Collectors.toList());

                return new ImmutableMethodTypeEncodedValue(new ImmutableMethodProtoReference(parameters, returnType));
            }
        });
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

    class Insn31tCell extends NonInitializedCell<Instruction> {
        private final Opcode opcode;
        private final int registerA;
        private final RWCell<Integer> offsetCell = new InnerNonInitializedCell<>(Integer.class, this);

        public Insn31tCell(Opcode opcode, int registerA) {
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
            return new ImmutableInstruction31t(opcode, registerA, offset);
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

    public class OffsetInsnRef {
        /**
         * The offset should be calculated relative to this instruction
         */
        private final RWCell<Instruction> relativeTo;

        /**
         * The offset cell should point at this instruction
         */
        private final RWCell<Instruction> target;

        /**
         * The cell containing the offset
         */
        private final RWCell<Integer> offsetCell;

        public OffsetInsnRef(RWCell<Instruction> relativeTo, RWCell<Instruction> target, RWCell<Integer> offsetCell) {
            this.relativeTo = relativeTo;
            this.target = target;
            this.offsetCell = offsetCell;
        }

        public RWCell<Instruction> getRelativeTo() {
            return relativeTo;
        }

        public RWCell<Instruction> getTarget() {
            return target;
        }

        public RWCell<Integer> getOffsetCell() {
            return offsetCell;
        }
    }

    // UTILS //

    private <T> T match(RefType type, RefTypeMatch<T> matcher) {
        return type instanceof ObjectType ? matcher.caseObject((ObjectType) type) :
                type instanceof ArrayType ? matcher.caseArray((ArrayType) type) :
                DexUtils.unreachable();
    }

    private <T> T match(PrimitiveType type, ILTypeMatch<T> matcher) {
        return type instanceof IntLikeType ? matcher.caseIntLike((IntLikeType) type) :
                type instanceof LongType ? matcher.caseLong() :
                DexUtils.unreachable();
    }

    private <T> T match(PrimitiveType type, FDTypeMatch<T> matcher) {
        return type instanceof FloatType ? matcher.caseFloat() :
                type instanceof DoubleType ? matcher.caseDouble() :
                DexUtils.unreachable();
    }

    private <T> T match(PrimitiveType type, ILFDTypeMatch<T> matcher) {
        return type instanceof IntLikeType ? matcher.caseIntLike((IntLikeType) type) :
                type instanceof LongType ? matcher.caseLong() :
                type instanceof FloatType ? matcher.caseFloat() :
                type instanceof DoubleType ? matcher.caseDouble() :
                DexUtils.unreachable();
    }

    private <T> T match(PrimitiveType type, PrimitiveTypeMatch<T> matcher) {
        return match(type, new ILFDTypeMatch<>() {
            @Override
            public T caseIntLike(IntLikeType type) {
                return type instanceof BooleanType ? matcher.caseBoolean() :
                        type instanceof ByteType ? matcher.caseByte() :
                        type instanceof ShortType ? matcher.caseShort() :
                        type instanceof CharType ? matcher.caseChar() :
                        type instanceof IntType ? matcher.caseInt() :
                        DexUtils.unreachable();
            }

            public T caseLong() { return matcher.caseLong(); }
            public T caseFloat() { return matcher.caseFloat(); }
            public T caseDouble() { return matcher.caseDouble(); }
        });
    }

    private <T> T match(Type type, TypeMatch<T> matcher) {
        return type instanceof PrimitiveType ? match((PrimitiveType) type, (PrimitiveTypeMatch<T>) matcher) :
                type instanceof RefType ? match((RefType) type, (RefTypeMatch<T>) matcher) :
                DexUtils.unreachable();
    }

    private <T> T match(Handle handle, HandleMatch<T> matcher) {
        if (handle instanceof FieldHandle) {
            return handle instanceof GetFieldHandle ? matcher.caseGetFieldHandle((GetFieldHandle) handle) :
                    handle instanceof SetFieldHandle ? matcher.caseSetFieldHandle((SetFieldHandle) handle) :
                    handle instanceof GetStaticHandle ? matcher.caseGetStaticHandle((GetStaticHandle) handle) :
                    handle instanceof SetStaticHandle ? matcher.caseSetStaticHandle((SetStaticHandle) handle) :
                    DexUtils.unreachable();
        } else if (handle instanceof MethodHandle) {
            return handle instanceof InvokeStaticHandle ? matcher.caseInvokeStaticHandle((InvokeStaticHandle) handle) :
                    handle instanceof InvokeInterfaceHandle ? matcher.caseInvokeInterfaceHandle((InvokeInterfaceHandle) handle) :
                    handle instanceof InvokeSpecialHandle ? matcher.caseInvokeSpecialHandle((InvokeSpecialHandle) handle) :
                    handle instanceof InvokeVirtualHandle ? matcher.caseInvokeVirtualHandle((InvokeVirtualHandle) handle) :
                    handle instanceof NewInstanceHandle ? matcher.caseNewInstanceHandle((NewInstanceHandle) handle) :
                    DexUtils.unreachable();
        } else {
            return DexUtils.unreachable();
        }
    }

    private <T> T match(BootstrapConstant constant, BootstrapConstantMatch<T> matcher) {
        return constant instanceof IntConstant ? matcher.caseIntConstant((IntConstant) constant) :
                constant instanceof LongConstant ? matcher.caseLongConstant((LongConstant) constant) :
                constant instanceof FloatConstant ? matcher.caseFloatConstant((FloatConstant) constant) :
                constant instanceof DoubleConstant ? matcher.caseDoubleConstant((DoubleConstant) constant) :
                constant instanceof StringConstant ? matcher.caseStringConstant((StringConstant) constant) :
                constant instanceof ClassConstant ? matcher.caseClassConstant((ClassConstant) constant) :
                constant instanceof HandleConstant ? matcher.caseHandleConstant((HandleConstant) constant) :
                constant instanceof MethodTypeConstant ? matcher.caseMethodTypeConstant((MethodTypeConstant) constant) :
                DexUtils.unreachable();
    }

    interface RefTypeMatch<T> {
        T caseArray(ArrayType type);
        T caseObject(ObjectType type);
    }

    interface ILTypeMatch<T> {
        T caseIntLike(IntLikeType type);
        T caseLong();
    }

    public interface FDTypeMatch<T> {
        T caseFloat();
        T caseDouble();
    }

    interface ILFDTypeMatch<T> {
        T caseIntLike(IntLikeType type);
        T caseLong();
        T caseFloat();
        T caseDouble();
    }

    interface PrimitiveTypeMatch<T> {
        T caseBoolean();
        T caseByte();
        T caseChar();
        T caseShort();
        T caseInt();
        T caseLong();
        T caseFloat();
        T caseDouble();
    }

    interface TypeMatch<T> extends PrimitiveTypeMatch<T>, RefTypeMatch<T> {}

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

    interface BootstrapConstantMatch<T> {
        T caseIntConstant(IntConstant constant);
        T caseLongConstant(LongConstant constant);
        T caseFloatConstant(FloatConstant constant);
        T caseDoubleConstant(DoubleConstant constant);
        T caseStringConstant(StringConstant constant);
        T caseClassConstant(ClassConstant constant);
        T caseHandleConstant(HandleConstant constant);
        T caseMethodTypeConstant(MethodTypeConstant constant);
    }
}
