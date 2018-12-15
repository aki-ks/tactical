package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.insn.MoveInstruction;
import me.aki.tactical.dex.utils.DexInsnVisitor;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction11x;

public class SmaliDexInsnReader {
    private final DexInsnVisitor<Instruction, Integer> iv;

    public SmaliDexInsnReader(DexInsnVisitor<Instruction, Integer> iv) {
        this.iv = iv;
    }

    public void accept(Instruction instruction) {
        switch (instruction.getOpcode()) {
            case NOP:
                break;

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
                OneRegisterInstruction insn = (OneRegisterInstruction) instruction;
                iv.visitMoveResult(DexType.NORMAL, insn.getRegisterA());
                break;
            }

            case MOVE_RESULT_WIDE: {
                OneRegisterInstruction insn = (OneRegisterInstruction) instruction;
                iv.visitMoveResult(DexType.WIDE, insn.getRegisterA());
                break;
            }

            case MOVE_RESULT_OBJECT: {
                OneRegisterInstruction insn = (OneRegisterInstruction) instruction;
                iv.visitMoveResult(DexType.OBJECT, insn.getRegisterA());
                break;
            }

            case MOVE_EXCEPTION: {
                OneRegisterInstruction insn = (OneRegisterInstruction) instruction;
                iv.visitMoveException(insn.getRegisterA());
                break;
            }

            case RETURN_VOID: {
                iv.visitReturnVoid();
                break;
            }

            case RETURN: {
                OneRegisterInstruction insn = (OneRegisterInstruction) instruction;
                iv.visitReturn(DexType.NORMAL, insn.getRegisterA());
                break;
            }
            case RETURN_WIDE: {
                OneRegisterInstruction insn = (OneRegisterInstruction) instruction;
                iv.visitReturn(DexType.WIDE, insn.getRegisterA());
                break;
            }
            case RETURN_OBJECT: {
                OneRegisterInstruction insn = (OneRegisterInstruction) instruction;
                iv.visitReturn(DexType.OBJECT, insn.getRegisterA());
                break;
            }

            case CONST_4:
            case CONST_16:
            case CONST:
            case CONST_HIGH16:
            case CONST_WIDE_16:
            case CONST_WIDE_32:
            case CONST_WIDE:
            case CONST_WIDE_HIGH16:
            case CONST_STRING:
            case CONST_STRING_JUMBO:
            case CONST_CLASS:
            case MONITOR_ENTER:
            case MONITOR_EXIT:
            case CHECK_CAST:
            case INSTANCE_OF:
            case ARRAY_LENGTH:
            case NEW_INSTANCE:
            case NEW_ARRAY:
            case FILLED_NEW_ARRAY:
            case FILLED_NEW_ARRAY_RANGE:
            case FILL_ARRAY_DATA:
            case THROW:
            case GOTO:
            case GOTO_16:
            case GOTO_32:
            case PACKED_SWITCH:
            case SPARSE_SWITCH:
            case CMPL_FLOAT:
            case CMPG_FLOAT:
            case CMPL_DOUBLE:
            case CMPG_DOUBLE:
            case CMP_LONG:
            case IF_EQ:
            case IF_NE:
            case IF_LT:
            case IF_GE:
            case IF_GT:
            case IF_LE:
            case IF_EQZ:
            case IF_NEZ:
            case IF_LTZ:
            case IF_GEZ:
            case IF_GTZ:
            case IF_LEZ:
            case AGET:
            case AGET_WIDE:
            case AGET_OBJECT:
            case AGET_BOOLEAN:
            case AGET_BYTE:
            case AGET_CHAR:
            case AGET_SHORT:
            case APUT:
            case APUT_WIDE:
            case APUT_OBJECT:
            case APUT_BOOLEAN:
            case APUT_BYTE:
            case APUT_CHAR:
            case APUT_SHORT:
            case IGET:
            case IGET_WIDE:
            case IGET_OBJECT:
            case IGET_BOOLEAN:
            case IGET_BYTE:
            case IGET_CHAR:
            case IGET_SHORT:
            case IPUT:
            case IPUT_WIDE:
            case IPUT_OBJECT:
            case IPUT_BOOLEAN:
            case IPUT_BYTE:
            case IPUT_CHAR:
            case IPUT_SHORT:
            case SGET:
            case SGET_WIDE:
            case SGET_OBJECT:
            case SGET_BOOLEAN:
            case SGET_BYTE:
            case SGET_CHAR:
            case SGET_SHORT:
            case SPUT:
            case SPUT_WIDE:
            case SPUT_OBJECT:
            case SPUT_BOOLEAN:
            case SPUT_BYTE:
            case SPUT_CHAR:
            case SPUT_SHORT:
            case INVOKE_VIRTUAL:
            case INVOKE_SUPER:
            case INVOKE_DIRECT:
            case INVOKE_STATIC:
            case INVOKE_INTERFACE:
            case INVOKE_VIRTUAL_RANGE:
            case INVOKE_SUPER_RANGE:
            case INVOKE_DIRECT_RANGE:
            case INVOKE_STATIC_RANGE:
            case INVOKE_INTERFACE_RANGE:
            case NEG_INT:
            case NOT_INT:
            case NEG_LONG:
            case NOT_LONG:
            case NEG_FLOAT:
            case NEG_DOUBLE:
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
            case INT_TO_SHORT:
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
            case REM_DOUBLE:
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
            case REM_DOUBLE_2ADDR:
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
            case USHR_INT_LIT8:
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
            case PACKED_SWITCH_PAYLOAD:
            case SPARSE_SWITCH_PAYLOAD:
            case ARRAY_PAYLOAD:
            case INVOKE_POLYMORPHIC:
            case INVOKE_POLYMORPHIC_RANGE:
            case INVOKE_CUSTOM:
            case INVOKE_CUSTOM_RANGE:
            case CONST_METHOD_HANDLE:
            case CONST_METHOD_TYPE:
            default:
                throw new RuntimeException("NOT YET IMPLEMENTED");
        }
    }
}
