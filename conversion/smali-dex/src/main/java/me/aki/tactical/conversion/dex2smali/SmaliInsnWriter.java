package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.conversion.dex2smali.provider.*;
import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.*;
import me.aki.tactical.core.constant.*;
import me.aki.tactical.core.handle.*;
import me.aki.tactical.core.type.*;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.FillArrayInstruction;
import me.aki.tactical.dex.insn.IfInstruction;
import me.aki.tactical.dex.insn.InvokeInstruction;
import me.aki.tactical.dex.insn.NewFilledArrayInstruction;
import me.aki.tactical.dex.invoke.Invoke;
import me.aki.tactical.dex.utils.DexCfgGraph;
import me.aki.tactical.dex.utils.DexInsnVisitor;
import org.jf.dexlib2.MethodHandleType;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.reference.*;
import org.jf.dexlib2.immutable.value.*;

import java.util.*;
import java.util.stream.Collectors;

public class SmaliInsnWriter extends DexInsnVisitor<me.aki.tactical.dex.insn.Instruction, Register> {
    /**
     * Return type of the method that contains the instruction the visited instructions.
     */
    private final Optional<Type> returnType;

    /**
     * Instruction that gets visited next.
     */
    private DexCfgGraph.Node instruction;

    private List<InstructionProvider<? extends Instruction>> instructions = new ArrayList<>();

    /**
     * Additional payload instructions that must get stored in the method as dead code.
     */
    private List<InstructionProvider<? extends Instruction>> payloadInstructions = new ArrayList<>();

    private final Map<Register, List<RegisterCell>> registerCells = new HashMap<>();
    private final List<OffsetCell> offsetCells = new ArrayList<>();

    /**
     * Require that registers are aligned next to each other to allow the generation of a range instruction.
     */
    private final List<RegisterConstraint> registerConstraints = new ArrayList<>();

    private int callSiteIndex = 0;

    public SmaliInsnWriter(Optional<Type> returnType) {
        this.returnType = returnType;
    }

    public void setInstruction(DexCfgGraph.Node instruction) {
        this.instruction = instruction;
    }

    private void visitInstruction(InstructionProvider<? extends Instruction> insn) {
        addCells(insn);
        this.instructions.add(insn);
    }

    private void visitPayloadInstruction(InstructionProvider<? extends Instruction> insn) {
        addCells(insn);
        this.payloadInstructions.add(insn);
    }

    private void addCells(InstructionProvider<? extends Instruction> insn) {
        for (RegisterCell registerCell : insn.getRegisterCells()) {
            this.registerCells.computeIfAbsent(registerCell.getRegister(), x -> new ArrayList<>()).add(registerCell);
        }

        this.offsetCells.addAll(insn.getOffsetCells());
    }

    public List<InstructionProvider<? extends Instruction>> getInstructions() {
        return instructions;
    }

    public List<InstructionProvider<? extends Instruction>> getPayloadInstructions() {
        return payloadInstructions;
    }

    @Override
    public void visitConstant(DexConstant constant, Register target) {
        if (constant instanceof ClassConstant) {
            RefType classConstant = ((ClassConstant) constant).getValue();
            String type = DexUtils.toDexType(classConstant);
            visitInstruction(new Insn21cProvider(Opcode.CONST_CLASS, target, new ImmutableTypeReference(type)));
        } else if (constant instanceof PrimitiveValueConstant) {
            if (constant instanceof IntConstant || constant instanceof FloatConstant) {
                visit32bitConstant((PrimitiveValueConstant) constant, target);
            } else if (constant instanceof LongConstant || constant instanceof DoubleConstant) {
                visit64bitConstant((PrimitiveValueConstant) constant, target);
            } else {
                DexUtils.unreachable();
            }
        } else if (constant instanceof HandleConstant) {
            MethodHandleReference reference = convertMethodHandle(((HandleConstant) constant).getHandle());
            visitInstruction(new Insn21cProvider(Opcode.CONST_METHOD_HANDLE, target, reference));
        } else if (constant instanceof MethodTypeConstant) {
            MethodTypeConstant methodType = (MethodTypeConstant) constant;
            String returnType = DexUtils.toDexReturnType(methodType.getReturnType());
            List<String> parameters = methodType.getArgumentTypes().stream()
                    .map(DexUtils::toDexType)
                    .collect(Collectors.toList());

            MethodProtoReference proto = new ImmutableMethodProtoReference(parameters, returnType);
            visitInstruction(new Insn21cProvider(Opcode.CONST_METHOD_TYPE, target, proto));
        } else if (constant instanceof StringConstant) {
            String string = ((StringConstant) constant).getValue();
            visitInstruction(new Insn21cProvider(Opcode.CONST_STRING, target, new ImmutableStringReference(string)));
        } else {
            DexUtils.unreachable();
        }
    }

    private void visit32bitConstant(PrimitiveValueConstant constant, Register target) {
        int literal = constant instanceof IntConstant ? ((IntConstant) constant).getValue() :
                constant instanceof FloatConstant ? Float.floatToRawIntBits(((FloatConstant) constant).getValue()) :
                DexUtils.unreachable();

        if (-8 <= literal && literal <= 7) {
            visitInstruction(new Insn11nProvider(Opcode.CONST_4, target, literal));
        } else if (Short.MIN_VALUE <= literal && literal <= Short.MAX_VALUE) {
            visitInstruction(new Insn21sProvider(Opcode.CONST_16, target, literal));
        } else if ((literal & 0x0000FFFF) == 0) {
            visitInstruction(new Insn21ihProvider(Opcode.CONST_HIGH16, target, literal));
        } else {
            visitInstruction(new Insn31iProvider(Opcode.CONST, target, literal));
        }
    }

    private void visit64bitConstant(PrimitiveValueConstant constant, Register target) {
        long literal = constant instanceof LongConstant ? ((LongConstant) constant).getValue() :
                constant instanceof DoubleConstant ? Double.doubleToLongBits(((DoubleConstant) constant).getValue()) :
                DexUtils.unreachable();

        if (Short.MIN_VALUE <= literal && literal <= Short.MAX_VALUE) {
            visitInstruction(new Insn21sProvider(Opcode.CONST_WIDE_16, target, (int) literal));
        } else if (Integer.MIN_VALUE <= literal && literal <= Integer.MAX_VALUE) {
            visitInstruction(new Insn31iProvider(Opcode.CONST_WIDE_32, target, (int) literal));
        } else if ((literal & 0x0000FFFFFFFFFFFL) == 0) {
            visitInstruction(new Insn21lhProvider(Opcode.CONST_WIDE_HIGH16, target, literal));
        } else {
            visitInstruction(new Insn51lProvider(Opcode.CONST_WIDE, target, literal));
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

    private void visitMathInsn(PrimitiveType type, Register op1, Register op2, Register result,
                               Opcode opInt, Opcode opLong, Opcode opFloat, Opcode opDouble,
                               Opcode opInt2addr, Opcode opLong2addr, Opcode opFloat2addr, Opcode opDouble2addr) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return opInt2addr; }
                public Opcode caseLong() { return opLong2addr; }
                public Opcode caseFloat() { return opFloat2addr; }
                public Opcode caseDouble() { return opDouble2addr; }
            });

            visitInstruction(new Insn12xProvider(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return opInt; }
                public Opcode caseLong() { return opLong; }
                public Opcode caseFloat() { return opFloat; }
                public Opcode caseDouble() { return opDouble; }
            });

            visitInstruction(new Insn23xProvider(opcode, result, op1, op2));
        }
    }

    private void visitMathInsn(PrimitiveType type, Register op1, Register op2, Register result, Opcode opInt, Opcode opLong, Opcode opInt2addr, Opcode opLong2addr) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return opInt2addr; }
                public Opcode caseLong() { return opLong2addr; }
            });

            visitInstruction(new Insn12xProvider(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return opInt; }
                public Opcode caseLong() { return opLong; }
            });

            visitInstruction(new Insn23xProvider(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitAdd(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.ADD_INT,
                Opcode.ADD_LONG,
                Opcode.ADD_FLOAT,
                Opcode.ADD_DOUBLE,
                Opcode.ADD_INT_2ADDR,
                Opcode.ADD_LONG_2ADDR,
                Opcode.ADD_FLOAT_2ADDR,
                Opcode.ADD_DOUBLE_2ADDR
        );
    }

    @Override
    public void visitSub(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.SUB_INT,
                Opcode.SUB_LONG,
                Opcode.SUB_FLOAT,
                Opcode.SUB_DOUBLE,
                Opcode.SUB_INT_2ADDR,
                Opcode.SUB_LONG_2ADDR,
                Opcode.SUB_FLOAT_2ADDR,
                Opcode.SUB_DOUBLE_2ADDR
        );
    }

    @Override
    public void visitMul(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.MUL_INT,
                Opcode.MUL_LONG,
                Opcode.MUL_FLOAT,
                Opcode.MUL_DOUBLE,
                Opcode.MUL_INT_2ADDR,
                Opcode.MUL_LONG_2ADDR,
                Opcode.MUL_FLOAT_2ADDR,
                Opcode.MUL_DOUBLE_2ADDR
        );
    }

    @Override
    public void visitDiv(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.DIV_INT,
                Opcode.DIV_LONG,
                Opcode.DIV_FLOAT,
                Opcode.DIV_DOUBLE,
                Opcode.DIV_INT_2ADDR,
                Opcode.DIV_LONG_2ADDR,
                Opcode.DIV_FLOAT_2ADDR,
                Opcode.DIV_DOUBLE_2ADDR
        );
    }

    @Override
    public void visitMod(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.REM_INT,
                Opcode.REM_LONG,
                Opcode.REM_FLOAT,
                Opcode.REM_DOUBLE,
                Opcode.REM_INT_2ADDR,
                Opcode.REM_LONG_2ADDR,
                Opcode.REM_FLOAT_2ADDR,
                Opcode.REM_DOUBLE_2ADDR
        );
    }

    @Override
    public void visitAnd(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.AND_INT,
                Opcode.AND_LONG,
                Opcode.AND_INT_2ADDR,
                Opcode.AND_LONG_2ADDR
        );
    }

    @Override
    public void visitOr(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.OR_INT,
                Opcode.OR_LONG,
                Opcode.OR_INT_2ADDR,
                Opcode.OR_LONG_2ADDR
        );
    }

    @Override
    public void visitXor(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.XOR_INT,
                Opcode.XOR_LONG,
                Opcode.XOR_INT_2ADDR,
                Opcode.XOR_LONG_2ADDR
        );
    }

    @Override
    public void visitShl(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.SHL_INT,
                Opcode.SHL_LONG,
                Opcode.SHL_INT_2ADDR,
                Opcode.SHL_LONG_2ADDR
        );
    }

    @Override
    public void visitShr(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.SHR_INT,
                Opcode.SHR_LONG,
                Opcode.SHR_INT_2ADDR,
                Opcode.SHR_LONG_2ADDR
        );
    }

    @Override
    public void visitUShr(PrimitiveType type, Register op1, Register op2, Register result) {
        visitMathInsn(type, op1, op2, result,
                Opcode.USHR_INT,
                Opcode.USHR_LONG,
                Opcode.USHR_INT_2ADDR,
                Opcode.USHR_LONG_2ADDR
        );
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

    @Override
    public void visitLitShl(Register op1, short literal, Register result) {
        visitLiteralMath(op1, literal, result, Opcode.SHL_INT_LIT8);
    }

    @Override
    public void visitLitShr(Register op1, short literal, Register result) {
        visitLiteralMath(op1, literal, result, Opcode.SHR_INT_LIT8);
    }

    @Override
    public void visitLitUShr(Register op1, short literal, Register result) {
        visitLiteralMath(op1, literal, result, Opcode.USHR_INT_LIT8);
    }

    private void visitLiteralMath(Register op1, short literal, Register result, Opcode ushrIntLit8) {
        visitInstruction(new Insn22bProvider(ushrIntLit8, result, op1, literal));
    }

    private void visitLiteralMathInsn(Opcode opLit8, Opcode opLit16, Register op1, short literal, Register result) {
        if (Byte.MIN_VALUE <= literal && literal <= Byte.MAX_VALUE) {
            visitLiteralMath(op1, literal, result, opLit8);
        } else {
            visitInstruction(new Insn22sProvider(opLit16, result, op1, literal));
        }
    }

    @Override
    public void visitNeg(PrimitiveType type, Register value, Register result) {
        Opcode opcode = match(type, new ILFDTypeMatch<>() {
            public Opcode caseIntLike(IntLikeType type) { return Opcode.NEG_INT; }
            public Opcode caseLong() { return Opcode.NEG_LONG; }
            public Opcode caseFloat() { return Opcode.NEG_FLOAT; }
            public Opcode caseDouble() { return Opcode.NEG_DOUBLE; }
        });

        visitInstruction(new Insn12xProvider(opcode, result, value));
    }

    @Override
    public void visitNot(PrimitiveType type, Register value, Register result) {
        Opcode opcode = match(type, new ILTypeMatch<>() {
            public Opcode caseIntLike(IntLikeType type) { return Opcode.NOT_INT; }
            public Opcode caseLong() { return Opcode.NOT_LONG; }
        });

        visitInstruction(new Insn12xProvider(opcode, result, value));
    }

    @Override
    public void visitCmp(Register op1, Register op2, Register result) {
        visitInstruction(new Insn23xProvider(Opcode.CMP_LONG, result, op1, op2));
    }

    @Override
    public void visitCmpl(PrimitiveType type, Register op1, Register op2, Register result) {
        Opcode opcode = match(type, new FDTypeMatch<>() {
            public Opcode caseFloat() { return Opcode.CMPL_FLOAT; }
            public Opcode caseDouble() { return Opcode.CMPL_DOUBLE; }
        });

        visitInstruction(new Insn23xProvider(opcode, result, op1, op2));
    }

    @Override
    public void visitCmpg(PrimitiveType type, Register op1, Register op2, Register result) {
        Opcode opcode = match(type, new FDTypeMatch<>() {
            public Opcode caseFloat() { return Opcode.CMPG_FLOAT; }
            public Opcode caseDouble() { return Opcode.CMPG_DOUBLE; }
        });

        visitInstruction(new Insn23xProvider(opcode, result, op1, op2));
    }

    // ARRAY INSTRUCTIONS //

    @Override
    public void visitArrayLength(Register array, Register result) {
        visitInstruction(new Insn12xProvider(Opcode.ARRAY_LENGTH, result, array));
    }

    @Override
    public void visitArrayLoad(Type type, Register array, Register index, Register result) {
        visitInstruction(new Insn23xProvider(getArrayLoadOpcode(type), result, array, index));
    }

    private Opcode getArrayLoadOpcode(Type type) {
        return match(type, new TypeMatch<>() {
            public Opcode caseBoolean() { return Opcode.AGET_BOOLEAN; }
            public Opcode caseByte() { return Opcode.AGET_BYTE; }
            public Opcode caseChar() { return Opcode.AGET_CHAR; }
            public Opcode caseShort() { return Opcode.AGET_SHORT; }
            public Opcode caseInt() { return Opcode.AGET;}
            public Opcode caseLong() { return Opcode.AGET_WIDE; }
            public Opcode caseFloat() { return Opcode.AGET; }
            public Opcode caseDouble() { return Opcode.AGET_WIDE; }
            public Opcode caseArray(ArrayType type) { return Opcode.AGET_OBJECT; }
            public Opcode caseObject(ObjectType type) { return Opcode.AGET_OBJECT; }
        });
    }

    @Override
    public void visitArrayStore(Type type, Register array, Register index, Register value) {
        visitInstruction(new Insn23xProvider(getArrayStoreOpcode(type), value, array, index));
    }

    private Opcode getArrayStoreOpcode(Type type) {
        return match(type, new TypeMatch<>() {
            public Opcode caseBoolean() { return Opcode.APUT_BOOLEAN; }
            public Opcode caseByte() { return Opcode.APUT_BYTE; }
            public Opcode caseChar() { return Opcode.APUT_CHAR; }
            public Opcode caseShort() { return Opcode.APUT_SHORT; }
            public Opcode caseInt() { return Opcode.APUT; }
            public Opcode caseLong() { return Opcode.APUT_WIDE; }
            public Opcode caseFloat() { return Opcode.APUT; }
            public Opcode caseDouble() { return Opcode.APUT_WIDE; }
            public Opcode caseArray(ArrayType type) { return Opcode.APUT_OBJECT; }
            public Opcode caseObject(ObjectType type) { return Opcode.APUT_OBJECT; }
        });
    }

    @Override
    public void visitFillArray(Register array, FillArrayInstruction.NumberSize elementSize, List<FillArrayInstruction.NumericConstant> values) {
        List<Number> numbers = values.stream()
                .map(FillArrayInstruction.NumericConstant::longValue)
                .collect(Collectors.toList());

        ArrayPayloadProvider arrayPayload = new ArrayPayloadProvider(elementSize.getByteSize(), numbers);

        visitPayloadInstruction(arrayPayload);
        visitInstruction(new Insn31tProvider(Opcode.FILL_ARRAY_DATA, array, arrayPayload));
    }

    @Override
    public void visitNewArray(ArrayType type, Register size, Register result) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toDexType(type));
        visitInstruction(new Insn22cProvider(Opcode.NEW_ARRAY, result, size, typeRef));
    }

    @Override
    public void visitNewFilledArray(ArrayType type, List<Register> registers) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toDexType(type));
        int registerCount = registers.size();
        if (registerCount <= 5) {
            Iterator<Register> registerIter = registers.iterator();
            Register registerC = registerIter.hasNext() ? registerIter.next() : null;
            Register registerD = registerIter.hasNext() ? registerIter.next() : null;
            Register registerE = registerIter.hasNext() ? registerIter.next() : null;
            Register registerF = registerIter.hasNext() ? registerIter.next() : null;
            Register registerG = registerIter.hasNext() ? registerIter.next() : null;
            visitInstruction(new Insn35cProvider(Opcode.FILLED_NEW_ARRAY, registerCount, registerC, registerD, registerE, registerF, registerG, typeRef));
        } else {
            Insn3rcProvider insn = new Insn3rcProvider(Opcode.FILLED_NEW_ARRAY_RANGE, registers.get(0), registerCount, typeRef);
            registerConstraints.add(new RegisterConstraint(insn, registers));
            visitInstruction(insn);
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

        visitInstruction(new Insn12xProvider(opcode, toRegister, fromRegister));
    }

    @Override
    public void visitRefCast(RefType type, Register register) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toDexType(type));
        visitInstruction(new Insn21cProvider(Opcode.CHECK_CAST, register, typeRef));
    }

    // MONITOR INSTRUCTIONS //

    @Override
    public void visitMonitorEnter(Register value) {
        visitInstruction(new Insn11xProvider(Opcode.MONITOR_ENTER, value));
    }

    @Override
    public void visitMonitorExit(Register value) {
        visitInstruction(new Insn11xProvider(Opcode.MONITOR_EXIT, value));
    }

    // INSTANCE OPERATIONS //

    @Override
    public void visitNew(Path type, Register result) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toObjectDescriptor(type));
        visitInstruction(new Insn21cProvider(Opcode.NEW_INSTANCE, result, typeRef));
    }

    @Override
    public void visitInstanceOf(RefType type, Register value, Register result) {
        TypeReference typeRef = new ImmutableTypeReference(DexUtils.toDexType(type));
        visitInstruction(new Insn22cProvider(Opcode.INSTANCE_OF, result, value, typeRef));
    }

    // METHOD EXIT INSTRUCTIONS //

    @Override
    public void visitReturn(Register register) {
        if (returnType.isPresent()) {
            visitInstruction(new Insn11xProvider(getReturnOpcode(returnType.get()), register));
        } else {
            throw new IllegalStateException("Void methods cannot return a value");
        }
    }

    private Opcode getReturnOpcode(Type type) {
        return match(type, new TypeMatch<>() {
            public Opcode caseBoolean() { return Opcode.RETURN; }
            public Opcode caseByte() { return Opcode.RETURN; }
            public Opcode caseChar() { return Opcode.RETURN; }
            public Opcode caseShort() { return Opcode.RETURN; }
            public Opcode caseInt() { return Opcode.RETURN; }
            public Opcode caseLong() { return Opcode.RETURN_WIDE; }
            public Opcode caseFloat() { return Opcode.RETURN; }
            public Opcode caseDouble() { return Opcode.RETURN_WIDE; }
            public Opcode caseArray(ArrayType type) { return Opcode.RETURN_OBJECT; }
            public Opcode caseObject(ObjectType type) { return Opcode.RETURN_OBJECT; }
        });
    }

    @Override
    public void visitReturnVoid() {
        visitInstruction(new Insn10xProvider(Opcode.RETURN_VOID));
    }

    @Override
    public void visitThrow(Register exception) {
        visitInstruction(new Insn11xProvider(Opcode.THROW, exception));
    }

    // FIELD ACCESS //

    @Override
    public void visitFieldGet(FieldRef field, Optional<Register> instanceOpt, Register result) {
        FieldReference fieldRef = DexUtils.convertFieldRef(field);
        boolean isInstance = instanceOpt.isPresent();

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
            visitInstruction(new Insn22cProvider(opcode, result, instanceOpt.get(), fieldRef));
        } else {
            visitInstruction(new Insn21cProvider(opcode, result, fieldRef));
        }
    }

    @Override
    public void visitFieldSet(FieldRef field, Optional<Register> instanceOpt, Register value) {
        FieldReference fieldRef = DexUtils.convertFieldRef(field);
        boolean isInstance = instanceOpt.isPresent();

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
            visitInstruction(new Insn22cProvider(opcode, value, instanceOpt.get(), fieldRef));
        } else {
            visitInstruction(new Insn21cProvider(opcode, value, fieldRef));
        }
    }

    @Override
    public void visitInvoke(InvokeType invoke, MethodRef method, Optional<Register> instanceOpt, List<Register> arguments) {
        MethodReference methodRef = DexUtils.convertMethodRef(method);

        List<Register> registers = concat(instanceOpt, arguments);
        int registerCount = registers.size();
        if (registerCount <= 5) {
            Opcode opcode = getInvokeOpcode(invoke);
            Iterator<Register> registerIter = registers.iterator();
            Register registerC = registerIter.hasNext() ? registerIter.next() : null;
            Register registerD = registerIter.hasNext() ? registerIter.next() : null;
            Register registerE = registerIter.hasNext() ? registerIter.next() : null;
            Register registerF = registerIter.hasNext() ? registerIter.next() : null;
            Register registerG = registerIter.hasNext() ? registerIter.next() : null;
            visitInstruction(new Insn35cProvider(opcode, registerCount, registerC, registerD, registerE, registerF, registerG, methodRef));
        } else {
            Opcode opcode = getInvokeRangeOpcode(invoke);
            Insn3rcProvider insn = new Insn3rcProvider(opcode, registers.get(0), registerCount, methodRef);
            registerConstraints.add(new RegisterConstraint(insn, registers));
            visitInstruction(insn);
        }
    }

    private <T> List<T> concat(Optional<T> headOpt, List<T> tail) {
        if (headOpt.isPresent()) {
            List<T> values = new ArrayList<>(tail.size() + 1);
            values.add(headOpt.get());
            values.addAll(tail);
            return values;
        } else {
            return new ArrayList<>(tail);
        }
    }

    private Opcode getInvokeOpcode(InvokeType invoke) {
        switch (invoke) {
            case DIRECT: return Opcode.INVOKE_DIRECT;
            case INTERFACE: return Opcode.INVOKE_INTERFACE;
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
            case STATIC: return Opcode.INVOKE_STATIC_RANGE;
            case SUPER: return Opcode.INVOKE_SUPER_RANGE;
            case VIRTUAL: return Opcode.INVOKE_VIRTUAL_RANGE;
            default: return DexUtils.unreachable();
        }
    }

    @Override
    public void visitPolymorphicInvoke(MethodRef method, MethodDescriptor descriptor, Register instance, List<Register> arguments) {
        MethodReference methodRef = DexUtils.convertMethodRef(method);
        MethodProtoReference methodProto = DexUtils.convertMethodProto(descriptor);

        List<Register> registers = new ArrayList<>(arguments.size() + 1);
        registers.add(instance);
        registers.addAll(arguments);

        int registerCount = registers.size();
        if (registerCount <= 5) {
            Iterator<Register> registerIter = registers.iterator();
            Register registerC = registerIter.hasNext() ? registerIter.next() : null;
            Register registerD = registerIter.hasNext() ? registerIter.next() : null;
            Register registerE = registerIter.hasNext() ? registerIter.next() : null;
            Register registerF = registerIter.hasNext() ? registerIter.next() : null;
            Register registerG = registerIter.hasNext() ? registerIter.next() : null;
            visitInstruction(new Insn45ccProvider(Opcode.INVOKE_POLYMORPHIC, registerCount, registerC, registerD, registerE, registerF, registerG, methodRef, methodProto));
        } else {
            Insn4rccProvider insn = new Insn4rccProvider(Opcode.INVOKE_POLYMORPHIC_RANGE, registers.get(0), registerCount, methodRef, methodProto);
            registerConstraints.add(new RegisterConstraint(insn, registers));
            visitInstruction(insn);
        }
    }

    @Override
    public void visitCustomInvoke(List<Register> arguments, String methodName, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
        List<EncodedValue> extraArguments = bootstrapArguments.stream()
                .map(this::convertBootstrapConstant)
                .collect(Collectors.toList());

        MethodProtoReference methodProto = DexUtils.convertMethodProto(descriptor);

        String callSiteName = Integer.toString(this.callSiteIndex++);
        CallSiteReference callSiteRef = new ImmutableCallSiteReference(callSiteName, convertMethodHandle(bootstrapMethod), methodName, methodProto, extraArguments);

        int registerCount = arguments.size();
        if (registerCount <= 5) {
            Iterator<Register> registerIter = arguments.iterator();
            Register registerC = registerIter.hasNext() ? registerIter.next() : null;
            Register registerD = registerIter.hasNext() ? registerIter.next() : null;
            Register registerE = registerIter.hasNext() ? registerIter.next() : null;
            Register registerF = registerIter.hasNext() ? registerIter.next() : null;
            Register registerG = registerIter.hasNext() ? registerIter.next() : null;
            visitInstruction(new Insn35cProvider(Opcode.INVOKE_CUSTOM, registerCount, registerC, registerD, registerE, registerF, registerG, callSiteRef));
        } else {
            Insn3rcProvider insn = new Insn3rcProvider(Opcode.INVOKE_CUSTOM_RANGE, arguments.get(0), registerCount, callSiteRef);
            registerConstraints.add(new RegisterConstraint(insn, arguments));
            visitInstruction(insn);
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

    // MOVE INSTRUCTIONS //

    @Override
    public void visitMove(Type type, Register from, Register to) {
        Opcode opcode;
        Opcode opcodeFrom16;
        Opcode opcode16;
        if (type instanceof ObjectType || type instanceof ArrayType) {
            opcode = Opcode.MOVE_OBJECT;
            opcodeFrom16 = Opcode.MOVE_OBJECT_FROM16;
            opcode16 = Opcode.MOVE_OBJECT_16;
        } else if (type instanceof DoubleType || type instanceof LongType) {
            opcode = Opcode.MOVE_WIDE;
            opcodeFrom16 = Opcode.MOVE_WIDE_FROM16;
            opcode16 = Opcode.MOVE_WIDE_16;
        } else if (type instanceof IntLikeType || type instanceof FloatType) {
            opcode = Opcode.MOVE;
            opcodeFrom16 = Opcode.MOVE_FROM16;
            opcode16 = Opcode.MOVE_16;
        } else {
            throw new RuntimeException("Unreachable");
        }

        visitInstruction(new MoveLikeInsnProvider(opcode, opcodeFrom16, opcode16, to, from));
    }

    @Override
    public void visitMoveResult(Register register) {
        List<DexCfgGraph.Node> preceedingNodes = instruction.getPreceeding();
        if (preceedingNodes.size() != 1) {
            throw new IllegalStateException("There should only be exactly one instruction preceding a move-result instruction");
        }

        Opcode opcode;
        me.aki.tactical.dex.insn.Instruction precedingInsn = preceedingNodes.get(0).getInstruction();
        if (precedingInsn instanceof NewFilledArrayInstruction) {
            opcode = Opcode.MOVE_RESULT_OBJECT;
        } else if (precedingInsn instanceof InvokeInstruction) {
            Invoke invoke = ((InvokeInstruction) precedingInsn).getInvoke();
            Optional<Type> returnType = invoke.getDescriptor().getReturnType();
            if (!returnType.isPresent()) {
                throw new IllegalStateException("Move-Result cannot succeed an invoke of a void");
            }

            opcode = match(returnType.get(), new TypeMatch<>() {
                public Opcode caseBoolean() { return Opcode.MOVE_RESULT; }
                public Opcode caseByte() { return Opcode.MOVE_RESULT; }
                public Opcode caseChar() { return Opcode.MOVE_RESULT; }
                public Opcode caseShort() { return Opcode.MOVE_RESULT; }
                public Opcode caseInt() { return Opcode.MOVE_RESULT; }
                public Opcode caseLong() { return Opcode.MOVE_RESULT_WIDE; }
                public Opcode caseFloat() { return Opcode.MOVE_RESULT; }
                public Opcode caseDouble() { return Opcode.MOVE_RESULT_WIDE; }
                public Opcode caseArray(ArrayType type) { return Opcode.MOVE_RESULT_OBJECT; }
                public Opcode caseObject(ObjectType type) { return Opcode.MOVE_RESULT_OBJECT; }
            });
        } else {
            throw new RuntimeException("Unreachable");
        }

        visitInstruction(new Insn11xProvider(opcode, register));
    }

    @Override
    public void visitMoveException(Register target) {
        visitInstruction(new Insn11xProvider(Opcode.MOVE_EXCEPTION, target));
    }

    // BRANCH INSTRUCTIONS //

    @Override
    public void visitGoto(me.aki.tactical.dex.insn.Instruction target) {
        visitInstruction(new GotoInsnProvider(target));
    }

    @Override
    public void visitIf(IfInstruction.Comparison comparison, Register op1, Optional<Register> op2Opt, me.aki.tactical.dex.insn.Instruction target) {
        if (op2Opt.isPresent()) {
            Register op2 = op2Opt.get();
            Opcode opcode = getTwoRegisterComparisonOpcode(comparison);
            visitInstruction(new Insn22tProvider(opcode, op1, op2, target));
        } else {
            Opcode opcode = getZeroComparisonOpcode(comparison);
            visitInstruction(new Insn21tProvider(opcode, op1, target));
        }
    }

    private Opcode getTwoRegisterComparisonOpcode(IfInstruction.Comparison comparison) {
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

    private Opcode getZeroComparisonOpcode(IfInstruction.Comparison comparison) {
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
        Opcode opcode;
        AbstractSwitchPayloadProvider<?> payload;

        TreeSet<Integer> sortedKeys = new TreeSet<>(branchTable.keySet());
        if (isRange(sortedKeys)) {
            opcode = Opcode.PACKED_SWITCH;
            payload = new PackedSwitchPayloadProvider(sortedKeys, branchTable);
        } else {
            opcode = Opcode.SPARSE_SWITCH;
            payload = new SparseSwitchPayloadProvider(sortedKeys, branchTable);
        }

        visitPayloadInstruction(payload);
        visitInstruction(new Insn31tProvider(opcode, value, payload));
    }

    private boolean isRange(TreeSet<Integer> sortedInts) {
        Integer last = null;
        for (Integer i : sortedInts) {
            if (last != null && last + 1 != i) {
                return false;
            }
            last = i;
        }
        return true;
    }

    // UTILS //

    public static class RegisterConstraint {
        /**
         * Instruction that requires the constraint.
         */
        private final InstructionProvider<?> instruction;

        private final List<Register> registers;

        public RegisterConstraint(InstructionProvider<?> instruction, List<Register> registers) {
            this.instruction = instruction;
            this.registers = registers;
        }

        public InstructionProvider<?> getInstruction() {
            return instruction;
        }

        public List<Register> getRegisters() {
            return registers;
        }
    }

    private <T> T match(Type type, RefTypeMatch<T> matcher) {
        return type instanceof ObjectType ? matcher.caseObject((ObjectType) type) :
                type instanceof ArrayType ? matcher.caseArray((ArrayType) type) :
                DexUtils.unreachable();
    }

    private <T> T match(Type type, ILTypeMatch<T> matcher) {
        return type instanceof IntLikeType ? matcher.caseIntLike((IntLikeType) type) :
                type instanceof LongType ? matcher.caseLong() :
                DexUtils.unreachable();
    }

    private <T> T match(Type type, FDTypeMatch<T> matcher) {
        return type instanceof FloatType ? matcher.caseFloat() :
                type instanceof DoubleType ? matcher.caseDouble() :
                DexUtils.unreachable();
    }

    private <T> T match(Type type, ILFDTypeMatch<T> matcher) {
        return type instanceof IntLikeType ? matcher.caseIntLike((IntLikeType) type) :
                type instanceof LongType ? matcher.caseLong() :
                type instanceof FloatType ? matcher.caseFloat() :
                type instanceof DoubleType ? matcher.caseDouble() :
                DexUtils.unreachable();
    }

    private <T> T match(Type type, PrimitiveTypeMatch<T> matcher) {
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
