package me.aki.tactical.dex.utils;

import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.insn.litmath.*;
import me.aki.tactical.dex.insn.math.*;

/**
 * Call events for {@link Instruction Instructions} on a {@link DexInsnVisitor}.
 */
public class DexInsnReader {
    private final DexInsnVisitor<Instruction, Register> iv;

    public DexInsnReader(DexInsnVisitor<Instruction, Register> iv) {
        this.iv = iv;
    }

    public void accept(Instruction instruction) {
        if (instruction instanceof AbstractBinaryMathInstruction) {
            if (instruction instanceof AddInstruction) {
                AddInstruction mathInsn = (AddInstruction) instruction;
                iv.visitAdd(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof SubInstruction) {
                SubInstruction mathInsn = (SubInstruction) instruction;
                iv.visitSub(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof MulInstruction) {
                MulInstruction mathInsn = (MulInstruction) instruction;
                iv.visitMul(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof DivInstruction) {
                DivInstruction mathInsn = (DivInstruction) instruction;
                iv.visitDiv(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof ModInstruction) {
                ModInstruction mathInsn = (ModInstruction) instruction;
                iv.visitMod(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof AndInstruction) {
                AndInstruction mathInsn = (AndInstruction) instruction;
                iv.visitAnd(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof OrInstruction) {
                OrInstruction mathInsn = (OrInstruction) instruction;
                iv.visitOr(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof XorInstruction) {
                XorInstruction mathInsn = (XorInstruction) instruction;
                iv.visitXor(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof ShlInstruction) {
                ShlInstruction mathInsn = (ShlInstruction) instruction;
                iv.visitShl(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof ShrInstruction) {
                ShrInstruction mathInsn = (ShrInstruction) instruction;
                iv.visitShr(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof UShrInstruction) {
                UShrInstruction mathInsn = (UShrInstruction) instruction;
                iv.visitUShr(mathInsn.getType(), mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else {
                throw new AssertionError();
            }
        } else if (instruction instanceof AbstractBinaryLitMathInstruction) {
            if (instruction instanceof AddLitInstruction) {
                AddLitInstruction mathInsn = (AddLitInstruction) instruction;
                iv.visitLitAdd(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof RSubLitInstruction) {
                RSubLitInstruction mathInsn = (RSubLitInstruction) instruction;
                iv.visitLitRSub(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof MulLitInstruction) {
                MulLitInstruction mathInsn = (MulLitInstruction) instruction;
                iv.visitLitMul(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof DivLitInstruction) {
                DivLitInstruction mathInsn = (DivLitInstruction) instruction;
                iv.visitLitDiv(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof ModLitInstruction) {
                ModLitInstruction mathInsn = (ModLitInstruction) instruction;
                iv.visitLitMod(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof AndLitInstruction) {
                AndLitInstruction mathInsn = (AndLitInstruction) instruction;
                iv.visitLitAnd(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof OrLitInstruction) {
                OrLitInstruction mathInsn = (OrLitInstruction) instruction;
                iv.visitLitOr(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof XorLitInstruction) {
                XorLitInstruction mathInsn = (XorLitInstruction) instruction;
                iv.visitLitXor(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof ShlLitInstruction) {
                ShlLitInstruction mathInsn = (ShlLitInstruction) instruction;
                iv.visitLitShl(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof ShrLitInstruction) {
                ShrLitInstruction mathInsn = (ShrLitInstruction) instruction;
                iv.visitLitShr(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else if (instruction instanceof UShrLitInstruction) {
                UShrLitInstruction mathInsn = (UShrLitInstruction) instruction;
                iv.visitLitUShr(mathInsn.getOp1(), mathInsn.getOp2(), mathInsn.getResult());
            } else {
                throw new AssertionError();
            }
        } else if (instruction instanceof AbstractCompareInstruction) {
            if (instruction instanceof CmpInstruction) {
                CmpInstruction cmp = (CmpInstruction) instruction;
                iv.visitCmp(cmp.getOp1(), cmp.getOp2(), cmp.getResult());
            } else if (instruction instanceof CmplInstruction) {
                CmplInstruction cmpl = (CmplInstruction) instruction;
                iv.visitCmpl(cmpl.getType(), cmpl.getOp1(), cmpl.getOp2(), cmpl.getResult());
            } else if (instruction instanceof CmpgInstruction) {
                CmpgInstruction cmpg = (CmpgInstruction) instruction;
                iv.visitCmpg(cmpg.getType(), cmpg.getOp1(), cmpg.getOp2(), cmpg.getResult());
            } else {
                throw new AssertionError();
            }
        } else if (instruction instanceof BranchInstruction) {
            if (instruction instanceof GotoInstruction) {
                iv.visitGoto(((GotoInstruction) instruction).getLocation());
            } else if (instruction instanceof IfInstruction) {
                IfInstruction ifInsn = (IfInstruction) instruction;
                iv.visitIf(ifInsn.getComparison(), ifInsn.getOp1(), ifInsn.getOp2(), ifInsn.getTarget());
            } else if (instruction instanceof SwitchInstruction) {
                SwitchInstruction switchInsn = (SwitchInstruction) instruction;
                iv.visitSwitch(switchInsn.getValue(), switchInsn.getBranchTable(), switchInsn.getDefaultBranch());
            } else {
                throw new AssertionError();
            }
        } else if (instruction instanceof ConstInstruction) {
            ConstInstruction constInsn = (ConstInstruction) instruction;
            iv.visitConstant(constInsn.getConstant());
        } else if (instruction instanceof ArrayLengthInstruction) {
            ArrayLengthInstruction lengthInsn = (ArrayLengthInstruction) instruction;
            iv.visitArrayLength(lengthInsn.getArray(), lengthInsn.getResult());
        } else if (instruction instanceof ArrayLoadInstruction) {
            ArrayLoadInstruction loadInsn = (ArrayLoadInstruction) instruction;
            iv.visitArrayLoad(loadInsn.getType(), loadInsn.getArray(), loadInsn.getIndex(), loadInsn.getResult());
        } else if (instruction instanceof ArrayStoreInstruction) {
            ArrayStoreInstruction storeInsn = (ArrayStoreInstruction) instruction;
            iv.visitArrayStore(storeInsn.getType(), storeInsn.getArray(), storeInsn.getIndex(), storeInsn.getIndex());
        } else if (instruction instanceof FieldGetInstruction) {
            FieldGetInstruction fieldInsn = (FieldGetInstruction) instruction;
            iv.visitFieldGet(fieldInsn.getField(), fieldInsn.getInstance(), fieldInsn.getResult());
        } else if (instruction instanceof FieldSetInstruction) {
            FieldSetInstruction fieldInsn = (FieldSetInstruction) instruction;
            iv.visitFieldSet(fieldInsn.getField(), fieldInsn.getInstance(), fieldInsn.getValue());
        } else if (instruction instanceof FillArrayInstruction) {
            FillArrayInstruction fillInsn = (FillArrayInstruction) instruction;
            iv.visitFillArray(fillInsn.getArray(), fillInsn.getValues());
        } else if (instruction instanceof InstanceOfInstruction) {
            InstanceOfInstruction instanceOfInsn = (InstanceOfInstruction) instruction;
            iv.visitInstanceOf(instanceOfInsn.getType(), instanceOfInsn.getValue(),instanceOfInsn.getResult());
        } else if (instruction instanceof InvokeInstruction) {
            InvokeInstruction invokeInsn = (InvokeInstruction) instruction;
            iv.visitInvoke(invokeInsn.getInvoke());
        } else if (instruction instanceof MonitorEnterInstruction) {
            MonitorEnterInstruction monitorEnter = (MonitorEnterInstruction) instruction;
            iv.visitMonitorEnterInstruction(monitorEnter.getRegister());
        } else if (instruction instanceof MonitorExitInstruction) {
            MonitorExitInstruction monitorInsn = (MonitorExitInstruction) instruction;
            iv.visitMonitorExitInstruction(monitorInsn.getRegister());
        } else if (instruction instanceof MoveExceptionInstruction) {
            MoveExceptionInstruction moveInsn = (MoveExceptionInstruction) instruction;
            iv.visitMoveException(moveInsn.getRegister());
        } else if (instruction instanceof MoveInstruction) {
            MoveInstruction moveInsn = (MoveInstruction) instruction;
            iv.visitMove(moveInsn.getType(), moveInsn.getFrom(), moveInsn.getTo());
        } else if (instruction instanceof MoveResultInstruction) {
            MoveResultInstruction moveInsn = (MoveResultInstruction) instruction;
            iv.visitMoveResult(moveInsn.getType(), moveInsn.getRegister());
        } else if (instruction instanceof NegInstruction) {
            NegInstruction newInsn = (NegInstruction) instruction;
            iv.visitNeg(newInsn.getType(), newInsn.getValue(), newInsn.getResult());
        } else if (instruction instanceof NewFilledArrayInstruction) {
            NewFilledArrayInstruction filledArrayInsn = (NewFilledArrayInstruction) instruction;
            iv.visitNewFilledArray(filledArrayInsn.getType(), filledArrayInsn.getRegisters());
        } else if (instruction instanceof NewInstanceInstruction) {
            NewInstanceInstruction newInsn = (NewInstanceInstruction) instruction;
            iv.visitNew(newInsn.getType(), newInsn.getResult());
        } else if (instruction instanceof NotInstruction) {
            NotInstruction notInsn = (NotInstruction) instruction;
            iv.visitNot(notInsn.getType(), notInsn.getValue(), notInsn.getResult());
        } else if (instruction instanceof PrimitiveCastInstruction) {
            PrimitiveCastInstruction castInsn = (PrimitiveCastInstruction) instruction;
            iv.visitPrimitiveCast(castInsn.getFromType(), castInsn.getToType(), castInsn.getFromRegister(), castInsn.getToRegister());
        } else if (instruction instanceof RefCastInstruction) {
            RefCastInstruction castInsn = (RefCastInstruction) instruction;
            iv.visitRefCast(castInsn.getType(), castInsn.getRegister());
        } else if (instruction instanceof ReturnInstruction) {
            ReturnInstruction returnInsn = (ReturnInstruction) instruction;
            iv.visitReturn(returnInsn.getType(), returnInsn.getRegister());
        } else if (instruction instanceof ReturnVoidInstruction) {
            iv.visitReturnVoid();
        } else if (instruction instanceof ThrowInstruction) {
            iv.visitThrow(((ThrowInstruction) instruction).getRegister());
        } else {
            throw new AssertionError();
        }
    }
}
