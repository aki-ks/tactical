package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.ClassConstant;
import me.aki.tactical.core.constant.DexNumberConstant;
import me.aki.tactical.core.constant.StringConstant;
import me.aki.tactical.core.type.*;
import me.aki.tactical.dex.DetailedDexType;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.insn.IfInstruction;
import me.aki.tactical.dex.invoke.Invoke;
import me.aki.tactical.dex.invoke.InvokeStatic;
import me.aki.tactical.dex.invoke.InvokeVirtual;
import me.aki.tactical.dex.utils.DexInsnVisitor;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.*;
import org.jf.dexlib2.iface.instruction.formats.*;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
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
            case MOVE_16: {
                TwoRegisterInstruction insn = (TwoRegisterInstruction) instruction;
                iv.visitMove(DexType.NORMAL, insn.getRegisterB(), insn.getRegisterA());
                break;
            }

            case MOVE_WIDE:
            case MOVE_WIDE_FROM16:
            case MOVE_WIDE_16: {
                TwoRegisterInstruction insn = (TwoRegisterInstruction) instruction;
                iv.visitMove(DexType.WIDE, insn.getRegisterB(), insn.getRegisterA());
                break;
            }

            case MOVE_OBJECT:
            case MOVE_OBJECT_FROM16:
            case MOVE_OBJECT_16: {
                TwoRegisterInstruction insn = (TwoRegisterInstruction) instruction;
                iv.visitMove(DexType.OBJECT, insn.getRegisterB(), insn.getRegisterA());
                break;
            }

            case MOVE_RESULT: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitMoveResult(DexType.NORMAL, insn.getRegisterA());
                break;
            }

            case MOVE_RESULT_WIDE: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitMoveResult(DexType.WIDE, insn.getRegisterA());
                break;
            }

            case MOVE_RESULT_OBJECT: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitMoveResult(DexType.OBJECT, insn.getRegisterA());
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

            case RETURN: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitReturn(DexType.NORMAL, insn.getRegisterA());
                break;
            }
            case RETURN_WIDE: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitReturn(DexType.WIDE, insn.getRegisterA());
                break;
            }
            case RETURN_OBJECT: {
                Instruction11x insn = (Instruction11x) instruction;
                iv.visitReturn(DexType.OBJECT, insn.getRegisterA());
                break;
            }

            // CONST //

            case CONST_4: {
                Instruction11n insn = (Instruction11n) instruction;
                iv.visitConstant(new DexNumberConstant(insn.getWideLiteral()), insn.getRegisterA());
                break;
            }
            case CONST_16: {
                Instruction21s insn = (Instruction21s) instruction;
                iv.visitConstant(new DexNumberConstant(insn.getWideLiteral()), insn.getRegisterA());
                break;
            }
            case CONST: {
                Instruction31i insn = (Instruction31i) instruction;
                iv.visitConstant(new DexNumberConstant(insn.getWideLiteral()), insn.getRegisterA());
                break;
            }
            case CONST_HIGH16: {
                Instruction21ih insn = (Instruction21ih) instruction;
                iv.visitConstant(new DexNumberConstant(insn.getWideLiteral()), insn.getRegisterA());
                break;
            }
            case CONST_WIDE_16: {
                Instruction21s insn = (Instruction21s) instruction;
                iv.visitConstant(new DexNumberConstant(insn.getWideLiteral()), insn.getRegisterA());
                break;
            }
            case CONST_WIDE_32: {
                Instruction31i insn = (Instruction31i) instruction;
                iv.visitConstant(new DexNumberConstant(insn.getWideLiteral()), insn.getRegisterA());
                break;
            }
            case CONST_WIDE: {
                Instruction51l insn = (Instruction51l) instruction;
                iv.visitConstant(new DexNumberConstant(insn.getWideLiteral()), insn.getRegisterA());
                break;
            }
            case CONST_WIDE_HIGH16: {
                Instruction21lh insn = (Instruction21lh) instruction;
                iv.visitConstant(new DexNumberConstant(insn.getWideLiteral()), insn.getRegisterA());
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

                String descriptor = ((TypeReference) insn.getReference()).getType();
                ArrayType type = (ArrayType) DexUtils.parseDescriptor(descriptor);

                List<Integer> registers = Stream.of(insn.getRegisterC(), insn.getRegisterD(), insn.getRegisterE(), insn.getRegisterF(), insn.getRegisterG())
                        .limit(insn.getRegisterCount())
                        .collect(Collectors.toList());

                iv.visitNewFilledArray(type, registers);
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

                List<DexNumberConstant> numbers = payload.getArrayElements().stream()
                        .map(Number::longValue)
                        .map(DexNumberConstant::new)
                        .collect(Collectors.toList());

                iv.visitFillArray(insn.getRegisterA(), numbers);
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

            case CMPL_FLOAT: {
                Instruction23x insn = (Instruction23x) instruction;
                iv.visitCmpl(FloatType.getInstance(), insn.getRegisterB(), insn.getRegisterC(), insn.getRegisterA());
                break;
            }
            case CMPG_FLOAT: {
                Instruction23x insn = (Instruction23x) instruction;
                iv.visitCmpg(FloatType.getInstance(), insn.getRegisterB(), insn.getRegisterC(), insn.getRegisterA());
                break;
            }
            case CMPL_DOUBLE: {
                Instruction23x insn = (Instruction23x) instruction;
                iv.visitCmpl(DoubleType.getInstance(), insn.getRegisterB(), insn.getRegisterC(), insn.getRegisterA());
                break;
            }
            case CMPG_DOUBLE: {
                Instruction23x insn = (Instruction23x) instruction;
                iv.visitCmpg(DoubleType.getInstance(), insn.getRegisterB(), insn.getRegisterC(), insn.getRegisterA());
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
                DetailedDexType type = getDetailedDexType(insn.getOpcode());
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
                DetailedDexType type = getDetailedDexType(insn.getOpcode());
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
                FieldRef field = toFieldRef((FieldReference) insn.getReference());
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
                FieldRef field = toFieldRef((FieldReference) insn.getReference());
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
                FieldRef field = toFieldRef((FieldReference) insn.getReference());
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
                FieldRef field = toFieldRef((FieldReference) insn.getReference());
                iv.visitFieldSet(field, Optional.empty(), insn.getRegisterA());
                break;
            }

            // UNARY MATH //

            case NEG_INT:
            case NEG_LONG:
            case NEG_FLOAT:
            case NEG_DOUBLE: {
                Instruction12x insn = (Instruction12x) instruction;
                iv.visitNeg(getPrimitiveType(insn.getOpcode()), insn.getRegisterB(), insn.getRegisterA());
                break;
            }

            case NOT_INT:
            case NOT_LONG: {
                Instruction12x insn = (Instruction12x) instruction;
                iv.visitNot(getPrimitiveType(insn.getOpcode()), insn.getRegisterB(), insn.getRegisterA());
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
                int literal = ((NarrowLiteralInstruction) instruction).getNarrowLiteral();
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
                visitInvokeInsn(insn);
                break;
            }

            case INVOKE_VIRTUAL_RANGE:
            case INVOKE_SUPER_RANGE:
            case INVOKE_DIRECT_RANGE:
            case INVOKE_STATIC_RANGE:
            case INVOKE_INTERFACE_RANGE:

            case INVOKE_POLYMORPHIC:
            case INVOKE_POLYMORPHIC_RANGE:

            case INVOKE_CUSTOM:
            case INVOKE_CUSTOM_RANGE:

            case CONST_METHOD_HANDLE:
            case CONST_METHOD_TYPE:
                throw new RuntimeException("NOT YET IMPLEMENTED");

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

    private DetailedDexType getDetailedDexType(Opcode opcode) {
        switch (opcode) {
            case AGET:
            case APUT:
                return DetailedDexType.NORMAL;

            case AGET_WIDE:
            case APUT_WIDE:
                return DetailedDexType.WIDE;

            case AGET_OBJECT:
            case APUT_OBJECT:
                return DetailedDexType.OBJECT;

            case AGET_BOOLEAN:
            case APUT_BOOLEAN:
                return DetailedDexType.BOOLEAN;

            case AGET_BYTE:
            case APUT_BYTE:
                return DetailedDexType.BYTE;

            case AGET_CHAR:
            case APUT_CHAR:
                return DetailedDexType.CHAR;

            case AGET_SHORT:
            case APUT_SHORT:
                return DetailedDexType.SHORT;

            default:
                throw new AssertionError();
        }
    }

    private PrimitiveType getPrimitiveType(Opcode opcode) {
        switch (opcode) {
            case NOT_INT:
            case NEG_INT:

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
                return IntType.getInstance();

            case NOT_LONG:
            case NEG_LONG:

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
                return LongType.getInstance();

            case NEG_FLOAT:

            case ADD_FLOAT:
            case SUB_FLOAT:
            case MUL_FLOAT:
            case DIV_FLOAT:
            case REM_FLOAT:

            case ADD_FLOAT_2ADDR:
            case SUB_FLOAT_2ADDR:
            case MUL_FLOAT_2ADDR:
            case DIV_FLOAT_2ADDR:
            case REM_FLOAT_2ADDR:
                return FloatType.getInstance();

            case NEG_DOUBLE:

            case ADD_DOUBLE:
            case SUB_DOUBLE:
            case MUL_DOUBLE:
            case DIV_DOUBLE:
            case REM_DOUBLE:

            case ADD_DOUBLE_2ADDR:
            case SUB_DOUBLE_2ADDR:
            case MUL_DOUBLE_2ADDR:
            case DIV_DOUBLE_2ADDR:
            case REM_DOUBLE_2ADDR:
                return DoubleType.getInstance();

            default:
                throw new AssertionError();
        }
    }

    private FieldRef toFieldRef(FieldReference reference) {
        Path owner = DexUtils.parseObjectDescriptor(reference.getDefiningClass());
        Type type = DexUtils.parseDescriptor(reference.getType());
        return new FieldRef(owner, reference.getName(), type);
    }

    private MethodRef toMethodRef(MethodReference reference) {
        Path owner = DexUtils.parseObjectDescriptor(reference.getDefiningClass());
        Optional<Type> returnType = DexUtils.parseReturnType(reference.getReturnType());
        List<Type> arguments = reference.getParameterTypes().stream()
                .map(DexUtils::parseDescriptor)
                .collect(Collectors.toList());

        return new MethodRef(owner, reference.getName(), arguments, returnType);
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
        PrimitiveType type = getPrimitiveType(instruction.getOpcode());
        Integer result = instruction.getRegisterA();
        Integer op1 = instruction.getRegisterA();
        Integer op2 = instruction.getRegisterB();

        visitMathInsn(instruction.getOpcode(), type, op1, op2, result);
    }

    private void visitMathInsn(Instruction23x instruction) {
        PrimitiveType type = getPrimitiveType(instruction.getOpcode());
        Integer result = instruction.getRegisterA();
        Integer op1 = instruction.getRegisterB();
        Integer op2 = instruction.getRegisterC();

        visitMathInsn(instruction.getOpcode(), type, op1, op2, result);
    }

    private void visitMathInsn(Opcode opcode, PrimitiveType type, Integer op1, Integer op2, Integer result) {
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

    private void visitLiteralMath(TwoRegisterInstruction insn, int literal) {
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

    private void visitInvokeInsn(Instruction35c insn) {
        List<Integer> registers = getRegisters(insn);
        MethodRef method = toMethodRef((MethodReference) insn.getReference());
        DexInsnVisitor.InvokeType invokeType = convertInvokeType(insn.getOpcode());

        Optional<Integer> instanceRegister;
        List<Integer> argumentRegisters;
        if (insn.getOpcode() == Opcode.INVOKE_STATIC) {
            instanceRegister = Optional.empty();
            argumentRegisters = new ArrayList<>(registers);
        } else {
            instanceRegister = Optional.of(registers.get(0));
            argumentRegisters = new ArrayList<>(registers.subList(1, registers.size()));
        }

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

            case INVOKE_POLYMORPHIC:
            case INVOKE_POLYMORPHIC_RANGE:
                return DexInsnVisitor.InvokeType.POLYMORPHIC;

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

    private List<Integer> getRegisters(Instruction35c insn) {
        switch (insn.getRegisterCount()) {
            case 0: return List.of();
            case 1: return List.of(insn.getRegisterC());
            case 2: return List.of(insn.getRegisterC(), insn.getRegisterD());
            case 3: return List.of(insn.getRegisterC(), insn.getRegisterD(), insn.getRegisterE());
            case 4: return List.of(insn.getRegisterC(), insn.getRegisterD(), insn.getRegisterE(), insn.getRegisterF());
            case 5: return List.of(insn.getRegisterC(), insn.getRegisterD(), insn.getRegisterE(), insn.getRegisterF(), insn.getRegisterG());
            default: throw new AssertionError();
        }
    }
}
