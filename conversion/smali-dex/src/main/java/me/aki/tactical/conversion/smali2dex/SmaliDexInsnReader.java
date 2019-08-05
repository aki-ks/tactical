package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.*;
import me.aki.tactical.core.handle.*;
import me.aki.tactical.core.type.*;
import me.aki.tactical.dex.insn.FillArrayInstruction;
import me.aki.tactical.dex.insn.IfInstruction;
import me.aki.tactical.dex.utils.DexInsnVisitor;
import org.jf.dexlib2.MethodHandleType;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.iface.instruction.*;
import org.jf.dexlib2.iface.instruction.formats.*;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.iface.value.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmaliDexInsnReader {
    private final InstructionIndex insnIndex;
    private final DexInsnVisitor<Instruction, Integer> iv;

    public SmaliDexInsnReader(InstructionIndex insnIndex, DexInsnVisitor<Instruction, Integer> iv) {
        this.insnIndex = insnIndex;
        this.iv = iv;
    }

    public void accept(Instruction instruction) {
        switch (instruction.getOpcode()) {
            case NOP:
                break;

            // MOVE //

            case MOVE:
            case MOVE_FROM16:
            case MOVE_16:

            case MOVE_WIDE:
            case MOVE_WIDE_FROM16:
            case MOVE_WIDE_16:

            case MOVE_OBJECT:
            case MOVE_OBJECT_FROM16:
            case MOVE_OBJECT_16: {
                TwoRegisterInstruction insn = (TwoRegisterInstruction) instruction;
                iv.visitMove(null, insn.getRegisterB(), insn.getRegisterA());
                break;
            }

            case MOVE_RESULT:
            case MOVE_RESULT_WIDE:
            case MOVE_RESULT_OBJECT: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitMoveResult(insn.getRegisterA());
                break;
            }

            case MOVE_EXCEPTION: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitMoveException(insn.getRegisterA());
                break;
            }

            // RETURN //

            case RETURN_VOID: {
                iv.visitReturnVoid();
                break;
            }

            case RETURN:
            case RETURN_WIDE:
            case RETURN_OBJECT: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitReturn(insn.getRegisterA());
                break;
            }

            // CONST //

            case CONST_4:
            case CONST_16:
            case CONST:
            case CONST_HIGH16:
            case CONST_WIDE_16:
            case CONST_WIDE_32:
            case CONST_WIDE:
            case CONST_WIDE_HIGH16: {
                int registerA = ((OneRegisterInstruction) instruction).getRegisterA();
                long literal = ((WideLiteralInstruction) instruction).getWideLiteral();
                iv.visitConstant(new UntypedNumberConstant(literal), registerA);
                break;
            }
            case CONST_STRING: {
                Instruction21c insn = (Instruction21c) instruction;
                String string = ((StringReference) insn.getReference()).getString();
                iv.visitConstant(new StringConstant(string), insn.getRegisterA());
                break;
            }
            case CONST_STRING_JUMBO: {
                Instruction31c insn = (Instruction31c) instruction;
                String string = ((StringReference) insn.getReference()).getString();
                iv.visitConstant(new StringConstant(string), insn.getRegisterA());
                break;
            }
            case CONST_CLASS: {
                Instruction21c insn = (Instruction21c) instruction;
                String descriptor = ((TypeReference) insn.getReference()).getType();
                RefType type = (RefType) DexUtils.parseDescriptor(descriptor);
                iv.visitConstant(new ClassConstant(type), insn.getRegisterA());
                break;
            }

            // MONITOR //

            case MONITOR_ENTER: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitMonitorEnter(insn.getRegisterA());
                break;
            }
            case MONITOR_EXIT: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitMonitorExit(insn.getRegisterA());
                break;
            }

            // OBJECT TYPE INSNS //

            case CHECK_CAST: {
                Instruction21c insn = (Instruction21c) instruction;
                String descriptor = ((TypeReference) insn.getReference()).getType();
                RefType type = (RefType) DexUtils.parseDescriptor(descriptor);
                iv.visitRefCast(type, insn.getRegisterA());
                break;
            }
            case INSTANCE_OF: {
                Instruction22c insn = (Instruction22c) instruction;
                String descriptor = ((TypeReference) insn.getReference()).getType();
                RefType type = (RefType) DexUtils.parseDescriptor(descriptor);
                iv.visitInstanceOf(type, insn.getRegisterB(), insn.getRegisterA());
                break;
            }
            case NEW_INSTANCE: {
                Instruction21c insn = (Instruction21c) instruction;
                String descriptor = ((TypeReference) insn.getReference()).getType();
                Path path = DexUtils.parseObjectDescriptor(descriptor);
                iv.visitNew(path, insn.getRegisterA());
                break;
            }

            // ARRAY INSNS //

            case ARRAY_LENGTH: {
                Instruction12x insn = (Instruction12x) instruction;
                iv.visitArrayLength(insn.getRegisterB(), insn.getRegisterA());
                break;
            }
            case NEW_ARRAY: {
                Instruction22c insn = (Instruction22c) instruction;
                String descriptor = ((TypeReference) insn.getReference()).getType();
                ArrayType type = (ArrayType) DexUtils.parseDescriptor(descriptor);
                iv.visitNewArray(type, insn.getRegisterB(), insn.getRegisterA());
                break;
            }
            case FILLED_NEW_ARRAY: {
                Instruction35c insn = (Instruction35c) instruction;

                String arrayDescriptor = ((TypeReference) insn.getReference()).getType();
                ArrayType type = (ArrayType) DexUtils.parseDescriptor(arrayDescriptor);

                iv.visitNewFilledArray(type, getRegisters(insn));
                break;
            }
            case FILLED_NEW_ARRAY_RANGE: {
                Instruction3rc insn = (Instruction3rc) instruction;

                String descriptor = ((TypeReference) insn.getReference()).getType();
                ArrayType type = (ArrayType) DexUtils.parseDescriptor(descriptor);

                List<Integer> registers = new ArrayList<>();
                for (int register = insn.getStartRegister(); register < insn.getRegisterCount(); register++) {
                    registers.add(register);
                }

                iv.visitNewFilledArray(type, registers);
                break;
            }
            case FILL_ARRAY_DATA: {
                Instruction31t insn = (Instruction31t) instruction;
                ArrayPayload payload = (ArrayPayload) insnIndex.getOffsetInstruction(insn, insn.getCodeOffset());
                FillArrayInstruction.NumberSize elementSize = FillArrayInstruction.NumberSize.fromByteSize(payload.getElementWidth());

                List<FillArrayInstruction.NumericConstant> numbers = payload.getArrayElements().stream()
                        .map(Number::longValue)
                        .map(FillArrayInstruction.NumericConstant::new)
                        .collect(Collectors.toList());

                iv.visitFillArray(insn.getRegisterA(), elementSize, numbers);
                break;
            }

            // THROW //

            case THROW: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitThrow(insn.getRegisterA());
                break;
            }

            // GOTO //

            case GOTO:
            case GOTO_16:
            case GOTO_32: {
                OffsetInstruction insn = (OffsetInstruction) instruction;
                iv.visitGoto(insnIndex.getOffsetInstruction(insn, insn.getCodeOffset()));
                break;
            }

            // SWITCH //

            case PACKED_SWITCH:
            case SPARSE_SWITCH: {
                Instruction31t insn = (Instruction31t) instruction;
                SwitchPayload payload = (SwitchPayload) insnIndex.getOffsetInstruction(insn, insn.getCodeOffset());

                LinkedHashMap<Integer, Instruction> branchTable = new LinkedHashMap<>();
                for (SwitchElement element : payload.getSwitchElements()) {
                    branchTable.put(element.getKey(), insnIndex.getOffsetInstruction(insn, element.getOffset()));
                }

                iv.visitSwitch(insn.getRegisterA(), branchTable);
                break;
            }

            // CMP //

            case CMPL_FLOAT:
            case CMPL_DOUBLE: {
                Instruction23x insn = (Instruction23x) instruction;
                PrimitiveType type = getTypeFromOpcode(insn.getOpcode());
                iv.visitCmpl(type, insn.getRegisterB(), insn.getRegisterC(), insn.getRegisterA());
                break;
            }

            case CMPG_FLOAT:
            case CMPG_DOUBLE: {
                Instruction23x insn = (Instruction23x) instruction;
                PrimitiveType type = getTypeFromOpcode(insn.getOpcode());
                iv.visitCmpg(type, insn.getRegisterB(), insn.getRegisterC(), insn.getRegisterA());
                break;
            }

            case CMP_LONG: {
                Instruction23x insn = (Instruction23x) instruction;
                iv.visitCmp(insn.getRegisterB(), insn.getRegisterC(), insn.getRegisterA());
                break;
            }

            // IF //

            case IF_EQ:
            case IF_NE:
            case IF_LT:
            case IF_GE:
            case IF_GT:
            case IF_LE:
                visitIfInstruction((Instruction22t) instruction);
                break;

            case IF_EQZ:
            case IF_NEZ:
            case IF_LTZ:
            case IF_GEZ:
            case IF_GTZ:
            case IF_LEZ:
                visitIfZeroInstruction((Instruction21t) instruction);
                break;

            // ARRAY ACCESS //

            case AGET:
            case AGET_WIDE:
            case AGET_OBJECT:
            case AGET_BOOLEAN:
            case AGET_BYTE:
            case AGET_CHAR:
            case AGET_SHORT: {
                Instruction23x insn = (Instruction23x) instruction;
                Type type = getTypeFromArrayAccessOpcode(insn.getOpcode());
                iv.visitArrayLoad(type, insn.getRegisterB(), insn.getRegisterC(), insn.getRegisterA());
                break;
            }

            case APUT:
            case APUT_WIDE:
            case APUT_OBJECT:
            case APUT_BOOLEAN:
            case APUT_BYTE:
            case APUT_CHAR:
            case APUT_SHORT: {
                Instruction23x insn = (Instruction23x) instruction;
                Type type = getTypeFromArrayAccessOpcode(insn.getOpcode());
                iv.visitArrayStore(type, insn.getRegisterB(), insn.getRegisterC(), insn.getRegisterA());
                break;
            }

            // FIELD ACCESS //

            case IGET:
            case IGET_WIDE:
            case IGET_OBJECT:
            case IGET_BOOLEAN:
            case IGET_BYTE:
            case IGET_CHAR:
            case IGET_SHORT: {
                Instruction22c insn = (Instruction22c) instruction;
                FieldRef field = DexUtils.toFieldRef((FieldReference) insn.getReference());
                iv.visitFieldGet(field, Optional.of(insn.getRegisterB()), insn.getRegisterA());
                break;
            }

            case IPUT:
            case IPUT_WIDE:
            case IPUT_OBJECT:
            case IPUT_BOOLEAN:
            case IPUT_BYTE:
            case IPUT_CHAR:
            case IPUT_SHORT: {
                Instruction22c insn = (Instruction22c) instruction;
                FieldRef field = DexUtils.toFieldRef((FieldReference) insn.getReference());
                iv.visitFieldSet(field, Optional.of(insn.getRegisterB()), insn.getRegisterA());
                break;
            }

            case SGET:
            case SGET_WIDE:
            case SGET_OBJECT:
            case SGET_BOOLEAN:
            case SGET_BYTE:
            case SGET_CHAR:
            case SGET_SHORT: {
                Instruction21c insn = (Instruction21c) instruction;
                FieldRef field = DexUtils.toFieldRef((FieldReference) insn.getReference());
                iv.visitFieldGet(field, Optional.empty(), insn.getRegisterA());
                break;
            }

            case SPUT:
            case SPUT_WIDE:
            case SPUT_OBJECT:
            case SPUT_BOOLEAN:
            case SPUT_BYTE:
            case SPUT_CHAR:
            case SPUT_SHORT: {
                Instruction21c insn = (Instruction21c) instruction;
                FieldRef field = DexUtils.toFieldRef((FieldReference) insn.getReference());
                iv.visitFieldSet(field, Optional.empty(), insn.getRegisterA());
                break;
            }

            // UNARY MATH //

            case NEG_INT:
            case NEG_LONG:
            case NEG_FLOAT:
            case NEG_DOUBLE: {
                Instruction12x insn = (Instruction12x) instruction;
                PrimitiveType type = getTypeFromOpcode(insn.getOpcode());
                iv.visitNeg(type, insn.getRegisterB(), insn.getRegisterA());
                break;
            }

            case NOT_INT:
            case NOT_LONG: {
                Instruction12x insn = (Instruction12x) instruction;
                PrimitiveType type = getTypeFromOpcode(insn.getOpcode());
                iv.visitNot(type, insn.getRegisterB(), insn.getRegisterA());
                break;
            }

            // PRIMITIVE CASTS //

            case INT_TO_LONG:
            case INT_TO_FLOAT:
            case INT_TO_DOUBLE:
            case LONG_TO_INT:
            case LONG_TO_FLOAT:
            case LONG_TO_DOUBLE:
            case FLOAT_TO_INT:
            case FLOAT_TO_LONG:
            case FLOAT_TO_DOUBLE:
            case DOUBLE_TO_INT:
            case DOUBLE_TO_LONG:
            case DOUBLE_TO_FLOAT:
            case INT_TO_BYTE:
            case INT_TO_CHAR:
            case INT_TO_SHORT: {
                visitPrimitiveCast((Instruction12x) instruction);
                break;
            }

            // MATH  //

            case ADD_INT:
            case SUB_INT:
            case MUL_INT:
            case DIV_INT:
            case REM_INT:
            case AND_INT:
            case OR_INT:
            case XOR_INT:
            case SHL_INT:
            case SHR_INT:
            case USHR_INT:

            case ADD_LONG:
            case SUB_LONG:
            case MUL_LONG:
            case DIV_LONG:
            case REM_LONG:
            case AND_LONG:
            case OR_LONG:
            case XOR_LONG:
            case SHL_LONG:
            case SHR_LONG:
            case USHR_LONG:

            case ADD_FLOAT:
            case SUB_FLOAT:
            case MUL_FLOAT:
            case DIV_FLOAT:
            case REM_FLOAT:

            case ADD_DOUBLE:
            case SUB_DOUBLE:
            case MUL_DOUBLE:
            case DIV_DOUBLE:
            case REM_DOUBLE: {
                visitMathInsn((Instruction23x) instruction);
                break;
            }

            // 2ADDR MATH //

            case ADD_INT_2ADDR:
            case SUB_INT_2ADDR:
            case MUL_INT_2ADDR:
            case DIV_INT_2ADDR:
            case REM_INT_2ADDR:
            case AND_INT_2ADDR:
            case OR_INT_2ADDR:
            case XOR_INT_2ADDR:
            case SHL_INT_2ADDR:
            case SHR_INT_2ADDR:
            case USHR_INT_2ADDR:

            case ADD_LONG_2ADDR:
            case SUB_LONG_2ADDR:
            case MUL_LONG_2ADDR:
            case DIV_LONG_2ADDR:
            case REM_LONG_2ADDR:
            case AND_LONG_2ADDR:
            case OR_LONG_2ADDR:
            case XOR_LONG_2ADDR:
            case SHL_LONG_2ADDR:
            case SHR_LONG_2ADDR:
            case USHR_LONG_2ADDR:

            case ADD_FLOAT_2ADDR:
            case SUB_FLOAT_2ADDR:
            case MUL_FLOAT_2ADDR:
            case DIV_FLOAT_2ADDR:
            case REM_FLOAT_2ADDR:

            case ADD_DOUBLE_2ADDR:
            case SUB_DOUBLE_2ADDR:
            case MUL_DOUBLE_2ADDR:
            case DIV_DOUBLE_2ADDR:
            case REM_DOUBLE_2ADDR: {
                visit2AddrMathInsn((Instruction12x) instruction);
                break;
            }

            // LITERAL MATH //

            case ADD_INT_LIT16:
            case RSUB_INT:
            case MUL_INT_LIT16:
            case DIV_INT_LIT16:
            case REM_INT_LIT16:
            case AND_INT_LIT16:
            case OR_INT_LIT16:
            case XOR_INT_LIT16:

            case ADD_INT_LIT8:
            case RSUB_INT_LIT8:
            case MUL_INT_LIT8:
            case DIV_INT_LIT8:
            case REM_INT_LIT8:
            case AND_INT_LIT8:
            case OR_INT_LIT8:
            case XOR_INT_LIT8:
            case SHL_INT_LIT8:
            case SHR_INT_LIT8:
            case USHR_INT_LIT8: {
                short literal = (short) ((NarrowLiteralInstruction) instruction).getNarrowLiteral();
                TwoRegisterInstruction insn = (TwoRegisterInstruction) instruction;
                visitLiteralMath(insn, literal);
                break;
            }

            // INVOKE //

            case INVOKE_VIRTUAL:
            case INVOKE_SUPER:
            case INVOKE_DIRECT:
            case INVOKE_STATIC:
            case INVOKE_INTERFACE: {
                Instruction35c insn = (Instruction35c) instruction;
                visitInvoke(insn, getRegisters(insn));
                break;
            }

            case INVOKE_VIRTUAL_RANGE:
            case INVOKE_SUPER_RANGE:
            case INVOKE_DIRECT_RANGE:
            case INVOKE_STATIC_RANGE:
            case INVOKE_INTERFACE_RANGE: {
                Instruction3rc insn = (Instruction3rc) instruction;
                visitInvoke(insn, getRegisters(insn));
                break;
            }

            case INVOKE_POLYMORPHIC: {
                Instruction45cc insn = (Instruction45cc) instruction;
                visitPolymorphicInvoke(insn, getRegisters(insn));
                break;
            }
            case INVOKE_POLYMORPHIC_RANGE: {
                Instruction4rcc insn = (Instruction4rcc) instruction;
                visitPolymorphicInvoke(insn, getRegisters(insn));
                break;
            }


            case INVOKE_CUSTOM: {
                Instruction35c insn = (Instruction35c) instruction;
                visitCustomInvoke(insn, getRegisters(insn));
                break;
            }
            case INVOKE_CUSTOM_RANGE: {
                Instruction3rc insn = (Instruction3rc) instruction;
                visitCustomInvoke(insn, getRegisters(insn));
                break;
            }

            case CONST_METHOD_HANDLE: {
                Instruction21c insn = (Instruction21c) instruction;
                MethodHandleReference reference = (MethodHandleReference) insn.getReference();
                Handle handle = convertMethodHandle(reference);
                iv.visitConstant(new HandleConstant(handle), insn.getRegisterA());
                break;
            }

            case CONST_METHOD_TYPE: {
                Instruction21c insn = (Instruction21c) instruction;
                MethodProtoReference proto = (MethodProtoReference) insn.getReference();
                MethodDescriptor descriptor = DexUtils.convertMethodDescriptor(proto);
                iv.visitConstant(new MethodTypeConstant(descriptor), insn.getRegisterA());
                break;
            }

            // PAYLOAD //

            case PACKED_SWITCH_PAYLOAD:
            case SPARSE_SWITCH_PAYLOAD:
            case ARRAY_PAYLOAD:
                // These instructions contain data and are only referenced by other instructions
                break;

            // ODEX INSTRUCTIONS ///

            case IGET_VOLATILE:
            case IPUT_VOLATILE:
            case SGET_VOLATILE:
            case SPUT_VOLATILE:
            case IGET_OBJECT_VOLATILE:
            case IGET_WIDE_VOLATILE:
            case IPUT_WIDE_VOLATILE:
            case SGET_WIDE_VOLATILE:
            case SPUT_WIDE_VOLATILE:

            case THROW_VERIFICATION_ERROR:
            case EXECUTE_INLINE:
            case EXECUTE_INLINE_RANGE:
            case INVOKE_DIRECT_EMPTY:
            case INVOKE_OBJECT_INIT_RANGE:
            case RETURN_VOID_BARRIER:
            case RETURN_VOID_NO_BARRIER:

            case IGET_QUICK:
            case IGET_WIDE_QUICK:
            case IGET_OBJECT_QUICK:
            case IPUT_QUICK:
            case IPUT_WIDE_QUICK:
            case IPUT_OBJECT_QUICK:
            case IPUT_BOOLEAN_QUICK:
            case IPUT_BYTE_QUICK:
            case IPUT_CHAR_QUICK:
            case IPUT_SHORT_QUICK:
            case IGET_BOOLEAN_QUICK:
            case IGET_BYTE_QUICK:
            case IGET_CHAR_QUICK:
            case IGET_SHORT_QUICK:

            case INVOKE_VIRTUAL_QUICK:
            case INVOKE_VIRTUAL_QUICK_RANGE:
            case INVOKE_SUPER_QUICK:
            case INVOKE_SUPER_QUICK_RANGE:
            case IPUT_OBJECT_VOLATILE:
            case SGET_OBJECT_VOLATILE:
            case SPUT_OBJECT_VOLATILE:
                // Baksmali should alread have deodexed the code, so there are no odex only instructions in the code
                throw new IllegalStateException("Unexpected ODEX instruction " + instruction.getOpcode());

            default:
                throw new AssertionError();
        }
    }

    private Type getTypeFromArrayAccessOpcode(Opcode opcode) {
        switch (opcode) {
            case AGET_BOOLEAN:
            case APUT_BOOLEAN:
                return BooleanType.getInstance();

            case AGET_BYTE:
            case APUT_BYTE:
                return ByteType.getInstance();

            case AGET_SHORT:
            case APUT_SHORT:
                return ShortType.getInstance();

            case AGET_CHAR:
            case APUT_CHAR:
                return CharType.getInstance();

            case AGET_OBJECT:
            case APUT_OBJECT:
                return ObjectType.OBJECT;

            case AGET:
            case APUT:
            case AGET_WIDE:
            case APUT_WIDE:
                // The type is not distinct, it gets set later by computing additional type informations.
                return null;

            default:
                return DexUtils.unreachable();
        }
    }

    private void visitIfInstruction(Instruction22t instruction) {
        Instruction target = insnIndex.getOffsetInstruction(instruction, instruction.getCodeOffset());
        iv.visitIf(getComparison(instruction.getOpcode()), instruction.getRegisterA(), Optional.of(instruction.getRegisterB()), target);
    }

    private void visitIfZeroInstruction(Instruction21t instruction) {
        Instruction target = insnIndex.getOffsetInstruction(instruction, instruction.getCodeOffset());
        iv.visitIf(getComparison(instruction.getOpcode()), instruction.getRegisterA(), Optional.empty(), target);
    }

    private IfInstruction.Comparison getComparison(Opcode opcode) {
        switch (opcode) {
            case IF_EQ:
            case IF_EQZ:
                return IfInstruction.Comparison.EQUAL;

            case IF_NE:
            case IF_NEZ:
                return IfInstruction.Comparison.NON_EQUAL;

            case IF_LT:
            case IF_LTZ:
                return IfInstruction.Comparison.LESS_THAN;

            case IF_GE:
            case IF_GEZ:
                return IfInstruction.Comparison.GREATER_EQUAL;

            case IF_GT:
            case IF_GTZ:
                return IfInstruction.Comparison.GREATER_THAN;

            case IF_LE:
            case IF_LEZ:
                return IfInstruction.Comparison.LESS_EQUAL;

            default:
                throw new AssertionError();
        }
    }

    private void visitPrimitiveCast(Instruction12x instruction) {
        PrimitiveType fromType;
        PrimitiveType toType;

        switch (instruction.getOpcode()) {
            case INT_TO_LONG:
            case INT_TO_FLOAT:
            case INT_TO_DOUBLE:
            case INT_TO_BYTE:
            case INT_TO_CHAR:
            case INT_TO_SHORT:
                fromType = IntType.getInstance();
                break;

            case LONG_TO_INT:
            case LONG_TO_FLOAT:
            case LONG_TO_DOUBLE:
                fromType = LongType.getInstance();
                break;

            case FLOAT_TO_INT:
            case FLOAT_TO_LONG:
            case FLOAT_TO_DOUBLE:
                fromType = FloatType.getInstance();
                break;

            case DOUBLE_TO_INT:
            case DOUBLE_TO_LONG:
            case DOUBLE_TO_FLOAT:
                fromType = DoubleType.getInstance();
                break;

            default:
                throw new AssertionError();
        }

        switch (instruction.getOpcode()) {
            case LONG_TO_INT:
            case FLOAT_TO_INT:
            case DOUBLE_TO_INT:
                toType = IntType.getInstance();
                break;

            case INT_TO_LONG:
            case FLOAT_TO_LONG:
            case DOUBLE_TO_LONG:
                toType = LongType.getInstance();
                break;

            case INT_TO_FLOAT:
            case LONG_TO_FLOAT:
            case DOUBLE_TO_FLOAT:
                toType = FloatType.getInstance();
                break;

            case INT_TO_DOUBLE:
            case LONG_TO_DOUBLE:
            case FLOAT_TO_DOUBLE:
                toType = DoubleType.getInstance();
                break;

            case INT_TO_BYTE:
                toType = ByteType.getInstance();
                break;

            case INT_TO_CHAR:
                toType = CharType.getInstance();
                break;

            case INT_TO_SHORT:
                toType = ShortType.getInstance();
                break;

            default:
                throw new AssertionError();
        }

        iv.visitPrimitiveCast(fromType, toType, instruction.getRegisterB(), instruction.getRegisterA());
    }

    private void visit2AddrMathInsn(Instruction12x instruction) {
        Integer result = instruction.getRegisterA();
        Integer op1 = instruction.getRegisterA();
        Integer op2 = instruction.getRegisterB();

        visitMathInsn(instruction.getOpcode(), op1, op2, result);
    }

    private void visitMathInsn(Instruction23x instruction) {
        Integer result = instruction.getRegisterA();
        Integer op1 = instruction.getRegisterB();
        Integer op2 = instruction.getRegisterC();

        visitMathInsn(instruction.getOpcode(), op1, op2, result);
    }

    private void visitMathInsn(Opcode opcode, Integer op1, Integer op2, Integer result) {
        PrimitiveType type = getTypeFromOpcode(opcode);
        switch (opcode) {
            case ADD_INT:
            case ADD_LONG:
            case ADD_FLOAT:
            case ADD_DOUBLE:
            case ADD_INT_2ADDR:
            case ADD_LONG_2ADDR:
            case ADD_FLOAT_2ADDR:
            case ADD_DOUBLE_2ADDR:
                iv.visitAdd(type, op1, op2, result);
                break;

            case SUB_INT:
            case SUB_LONG:
            case SUB_FLOAT:
            case SUB_DOUBLE:
            case SUB_INT_2ADDR:
            case SUB_LONG_2ADDR:
            case SUB_FLOAT_2ADDR:
            case SUB_DOUBLE_2ADDR:
                iv.visitSub(type, op1, op2, result);
                break;

            case MUL_INT:
            case MUL_LONG:
            case MUL_FLOAT:
            case MUL_DOUBLE:
            case MUL_INT_2ADDR:
            case MUL_LONG_2ADDR:
            case MUL_FLOAT_2ADDR:
            case MUL_DOUBLE_2ADDR:
                iv.visitMul(type, op1, op2, result);
                break;

            case DIV_INT:
            case DIV_LONG:
            case DIV_FLOAT:
            case DIV_DOUBLE:
            case DIV_INT_2ADDR:
            case DIV_LONG_2ADDR:
            case DIV_FLOAT_2ADDR:
            case DIV_DOUBLE_2ADDR:
                iv.visitDiv(type, op1, op2, result);
                break;

            case REM_INT:
            case REM_LONG:
            case REM_FLOAT:
            case REM_DOUBLE:
            case REM_INT_2ADDR:
            case REM_LONG_2ADDR:
            case REM_FLOAT_2ADDR:
            case REM_DOUBLE_2ADDR:
                iv.visitMod(type, op1, op2, result);
                break;

            case AND_INT:
            case AND_LONG:
            case AND_INT_2ADDR:
            case AND_LONG_2ADDR:
                iv.visitAnd(type, op1, op2, result);
                break;

            case OR_INT:
            case OR_LONG:
            case OR_INT_2ADDR:
            case OR_LONG_2ADDR:
                iv.visitOr(type, op1, op2, result);
                break;

            case XOR_INT:
            case XOR_LONG:
            case XOR_INT_2ADDR:
            case XOR_LONG_2ADDR:
                iv.visitXor(type, op1, op2, result);
                break;

            case SHL_INT:
            case SHL_LONG:
            case SHL_INT_2ADDR:
            case SHL_LONG_2ADDR:
                iv.visitShl(type, op1, op2, result);
                break;

            case SHR_INT:
            case SHR_LONG:
            case SHR_INT_2ADDR:
            case SHR_LONG_2ADDR:
                iv.visitShr(type, op1, op2 ,result);
                break;

            case USHR_INT:
            case USHR_LONG:
            case USHR_INT_2ADDR:
            case USHR_LONG_2ADDR:
                iv.visitUShr(type, op1, op2, result);
                break;

            default:
                throw new AssertionError();
        }
    }

    private PrimitiveType getTypeFromOpcode(Opcode opcode) {
        switch (opcode) {
            case ADD_INT:
            case ADD_INT_2ADDR:
            case SUB_INT:
            case SUB_INT_2ADDR:
            case MUL_INT:
            case MUL_INT_2ADDR:
            case DIV_INT:
            case DIV_INT_2ADDR:
            case REM_INT:
            case REM_INT_2ADDR:
            case AND_INT:
            case AND_INT_2ADDR:
            case OR_INT:
            case OR_INT_2ADDR:
            case XOR_INT:
            case XOR_INT_2ADDR:
            case SHL_INT:
            case SHL_INT_2ADDR:
            case SHR_INT:
            case SHR_INT_2ADDR:
            case USHR_INT:
            case USHR_INT_2ADDR:
            case NEG_INT:
            case NOT_INT:
                return IntType.getInstance();

            case ADD_LONG:
            case ADD_LONG_2ADDR:
            case SUB_LONG:
            case SUB_LONG_2ADDR:
            case MUL_LONG:
            case MUL_LONG_2ADDR:
            case DIV_LONG:
            case DIV_LONG_2ADDR:
            case REM_LONG:
            case REM_LONG_2ADDR:
            case AND_LONG:
            case AND_LONG_2ADDR:
            case OR_LONG:
            case OR_LONG_2ADDR:
            case XOR_LONG:
            case XOR_LONG_2ADDR:
            case SHL_LONG:
            case SHL_LONG_2ADDR:
            case SHR_LONG:
            case SHR_LONG_2ADDR:
            case USHR_LONG:
            case USHR_LONG_2ADDR:
            case NEG_LONG:
            case NOT_LONG:
                return LongType.getInstance();

            case ADD_FLOAT:
            case ADD_FLOAT_2ADDR:
            case SUB_FLOAT:
            case SUB_FLOAT_2ADDR:
            case MUL_FLOAT:
            case MUL_FLOAT_2ADDR:
            case DIV_FLOAT:
            case DIV_FLOAT_2ADDR:
            case REM_FLOAT:
            case REM_FLOAT_2ADDR:
            case NEG_FLOAT:
            case CMPL_FLOAT:
            case CMPG_FLOAT:
                return FloatType.getInstance();

            case ADD_DOUBLE:
            case ADD_DOUBLE_2ADDR:
            case SUB_DOUBLE:
            case SUB_DOUBLE_2ADDR:
            case MUL_DOUBLE:
            case MUL_DOUBLE_2ADDR:
            case DIV_DOUBLE:
            case DIV_DOUBLE_2ADDR:
            case REM_DOUBLE:
            case REM_DOUBLE_2ADDR:
            case NEG_DOUBLE:
            case CMPL_DOUBLE:
            case CMPG_DOUBLE:
                return DoubleType.getInstance();

            default:
                return DexUtils.unreachable();
        }
    }

    private void visitLiteralMath(TwoRegisterInstruction insn, short literal) {
        Integer op1 = insn.getRegisterB();
        Integer result = insn.getRegisterA();

        switch (insn.getOpcode()) {
            case ADD_INT_LIT8:
            case ADD_INT_LIT16:
                iv.visitLitAdd(op1, literal, result);
                break;

            case RSUB_INT_LIT8:
            case RSUB_INT:
                iv.visitLitRSub(op1, literal, result);
                break;

            case MUL_INT_LIT8:
            case MUL_INT_LIT16:
                iv.visitLitMul(op1, literal, result);
                break;

            case DIV_INT_LIT8:
            case DIV_INT_LIT16:
                iv.visitLitDiv(op1, literal, result);
                break;

            case REM_INT_LIT8:
            case REM_INT_LIT16:
                iv.visitLitMod(op1, literal, result);
                break;

            case AND_INT_LIT8:
            case AND_INT_LIT16:
                iv.visitLitAnd(op1, literal, result);
                break;

            case OR_INT_LIT8:
            case OR_INT_LIT16:
                iv.visitLitOr(op1, literal, result);
                break;

            case XOR_INT_LIT8:
            case XOR_INT_LIT16:
                iv.visitLitXor(op1, literal, result);
                break;

            case SHL_INT_LIT8:
                iv.visitLitShl(op1, literal, result);
                break;

            case SHR_INT_LIT8:
                iv.visitLitShr(op1, literal, result);
                break;

            case USHR_INT_LIT8:
                iv.visitLitUShr(op1, literal, result);
                break;

            default:
                throw new AssertionError();
        }
    }

    private List<Integer> getRegisters(FiveRegisterInstruction insn) {
        return Stream.of(insn.getRegisterC(), insn.getRegisterD(), insn.getRegisterE(), insn.getRegisterF(), insn.getRegisterG())
                        .limit(insn.getRegisterCount())
                        .collect(Collectors.toList());
    }

    private List<Integer> getRegisters(RegisterRangeInstruction insn) {
        List<Integer> registers = new ArrayList<>(insn.getRegisterCount());
        for (int i = 0; i < insn.getRegisterCount(); i++) {
            registers.add(insn.getStartRegister() + i);
        }
        return registers;
    }

    private void visitInvoke(ReferenceInstruction insn, List<Integer> registers) {
        MethodRef method = DexUtils.toMethodRef((MethodReference) insn.getReference());
        DexInsnVisitor.InvokeType invokeType = convertInvokeType(insn.getOpcode());

        Optional<Integer> instanceRegister;
        List<Integer> argumentRegisters;
        switch (insn.getOpcode()) {
            case INVOKE_STATIC:
            case INVOKE_STATIC_RANGE:
                instanceRegister = Optional.empty();
                argumentRegisters = new ArrayList<>(registers);
                break;

            default:
                instanceRegister = Optional.of(registers.get(0));
                argumentRegisters = new ArrayList<>(registers.subList(1, registers.size()));
                break;
        }

        dropDummyRegisters(method.getArguments(), argumentRegisters);

        iv.visitInvoke(invokeType, method, instanceRegister, argumentRegisters);
    }

    private DexInsnVisitor.InvokeType convertInvokeType(Opcode opcode) {
        switch (opcode) {
            case INVOKE_DIRECT:
            case INVOKE_DIRECT_RANGE:
                return DexInsnVisitor.InvokeType.DIRECT;

            case INVOKE_INTERFACE:
            case INVOKE_INTERFACE_RANGE:
                return DexInsnVisitor.InvokeType.INTERFACE;

            case INVOKE_STATIC:
            case INVOKE_STATIC_RANGE:
                return DexInsnVisitor.InvokeType.STATIC;

            case INVOKE_SUPER:
            case INVOKE_SUPER_RANGE:
                return DexInsnVisitor.InvokeType.SUPER;

            case INVOKE_VIRTUAL:
            case INVOKE_VIRTUAL_RANGE:
                return DexInsnVisitor.InvokeType.VIRTUAL;

            default:
                throw new AssertionError();
        }
    }

    private void visitPolymorphicInvoke(DualReferenceInstruction insn, List<Integer> registers) {
        MethodRef method = DexUtils.toMethodRef((MethodReference) insn.getReference());
        MethodDescriptor descriptor = DexUtils.convertMethodDescriptor((MethodProtoReference) insn.getReference2());

        Integer instance = registers.get(0);
        List<Integer> arguments = registers.subList(1, registers.size());

        dropDummyRegisters(descriptor.getParameterTypes(), arguments);

        iv.visitPolymorphicInvoke(method, descriptor, instance, arguments);
    }

    private void visitCustomInvoke(ReferenceInstruction insn, List<Integer> arguments) {
        CallSiteReference reference = (CallSiteReference) insn.getReference();

        Handle bootstrapMethod = convertMethodHandle(reference.getMethodHandle());
        MethodDescriptor descriptor = DexUtils.convertMethodDescriptor(reference.getMethodProto());
        List<BootstrapConstant> bootstrapArguments = reference.getExtraArguments().stream()
                .map(this::convertBootstrapConstant)
                .collect(Collectors.toList());

        dropDummyRegisters(descriptor.getParameterTypes(), arguments);

        iv.visitCustomInvoke(arguments, reference.getMethodName(), descriptor, bootstrapArguments, bootstrapMethod);
    }

    /**
     * Remove the dummy registers for longs and doubles from a list of registers
     *
     * @param types the list of types
     * @param registers the list of corresponding types
     */
    private void dropDummyRegisters(List<Type> types, List<Integer> registers) {
        Iterator<Type> typeIter = types.iterator();
        Iterator<Integer> registerIter = registers.iterator();

        while (typeIter.hasNext()) {
            Type type = typeIter.next();
            registerIter.next();

            if (type instanceof LongType || type instanceof DoubleType) {
                // Remove the following local, which represents the 2nd part of the long/double
                registerIter.next();
                registerIter.remove();
            }
        }

        // After all dummy registers have been removed,
        // the register count should match the local count
        if (types.size() != registers.size()) {
            throw new IllegalStateException("types=" + types + ", registers=" + registers);
        }
    }

    private BootstrapConstant convertBootstrapConstant(EncodedValue value) {
        switch (value.getValueType()) {
            case ValueType.BOOLEAN: return new IntConstant(((BooleanEncodedValue) value).getValue() ? 1 : 0);
            case ValueType.BYTE: return new IntConstant(((ByteEncodedValue) value).getValue());
            case ValueType.SHORT: return new IntConstant(((ShortEncodedValue) value).getValue());
            case ValueType.CHAR: return new IntConstant(((CharEncodedValue) value).getValue());
            case ValueType.INT: return new IntConstant(((IntEncodedValue) value).getValue());
            case ValueType.LONG: return new LongConstant(((LongEncodedValue) value).getValue());
            case ValueType.FLOAT: return new FloatConstant(((FloatEncodedValue) value).getValue());
            case ValueType.DOUBLE: return new DoubleConstant(((DoubleEncodedValue) value).getValue());

            case ValueType.STRING:
                return new StringConstant(((StringEncodedValue) value).getValue());

            case ValueType.TYPE:
                Type type = DexUtils.parseDescriptor(((TypeEncodedValue) value).getValue());
                return new ClassConstant((RefType) type);

            case ValueType.METHOD_TYPE:
                MethodProtoReference proto = ((MethodTypeEncodedValue) value).getValue();
                return new MethodTypeConstant(DexUtils.convertMethodDescriptor(proto));

            case ValueType.METHOD_HANDLE:
                MethodHandleReference handle = ((MethodHandleEncodedValue) value).getValue();
                return new HandleConstant(convertMethodHandle(handle));

            case ValueType.ENUM:
            case ValueType.ARRAY:
            case ValueType.ANNOTATION:
            case ValueType.FIELD:
            case ValueType.METHOD:
            case ValueType.NULL:
            default:
                throw new AssertionError(ValueType.getValueTypeName(value.getValueType()));
        }
    }

    private Handle convertMethodHandle(MethodHandleReference methodHandle) {
        switch (methodHandle.getMethodHandleType()) {
            case MethodHandleType.STATIC_PUT:
            case MethodHandleType.STATIC_GET:
            case MethodHandleType.INSTANCE_PUT:
            case MethodHandleType.INSTANCE_GET:
                FieldRef fieldRef = DexUtils.toFieldRef((FieldReference) methodHandle.getMemberReference());
                switch (methodHandle.getMethodHandleType()) {
                    case MethodHandleType.STATIC_PUT: return new SetStaticHandle(fieldRef);
                    case MethodHandleType.STATIC_GET: return new GetStaticHandle(fieldRef);
                    case MethodHandleType.INSTANCE_PUT: return new SetFieldHandle(fieldRef);
                    case MethodHandleType.INSTANCE_GET: return new GetFieldHandle(fieldRef);
                    default: throw new AssertionError();
                }

            case MethodHandleType.INVOKE_STATIC:
            case MethodHandleType.INVOKE_INSTANCE:
            case MethodHandleType.INVOKE_CONSTRUCTOR:
            case MethodHandleType.INVOKE_DIRECT:
            case MethodHandleType.INVOKE_INTERFACE:
                MethodRef methodRef = DexUtils.toMethodRef((MethodReference) methodHandle.getMemberReference());
                switch (methodHandle.getMethodHandleType()) {
                    case MethodHandleType.INVOKE_STATIC: return new InvokeStaticHandle(methodRef, false);
                    case MethodHandleType.INVOKE_INSTANCE: return new InvokeVirtualHandle(methodRef);
                    case MethodHandleType.INVOKE_CONSTRUCTOR: return new NewInstanceHandle(methodRef);
                    case MethodHandleType.INVOKE_DIRECT: return new InvokeSpecialHandle(methodRef, false);
                    case MethodHandleType.INVOKE_INTERFACE: return new InvokeInterfaceHandle(methodRef);
                    default: throw new AssertionError();
                }

            default:
                throw new AssertionError();
        }
    }
}
