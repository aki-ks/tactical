package me.aki.tactical.dex.utils;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.core.constant.DexNumberConstant;
import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.dex.DetailedDexType;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.insn.IfInstruction;
import me.aki.tactical.dex.insn.Instruction;
import me.aki.tactical.dex.invoke.Invoke;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * A visitor that visits instructions for a dalvik vm in a way similar to the dex {@link Instruction Instructions}.
 *
 * @param <I> representation for references to other instructions
 * @param <R> representation for registers
 */
public class DexInsnVisitor<I, R> {
    private final DexInsnVisitor<I, R> iv;

    public DexInsnVisitor(DexInsnVisitor<I, R> iv) {
        this.iv = iv;
    }

    public void visitConstant(DexConstant constant, R target) {
        if (iv != null) {
            iv.visitConstant(constant, target);
        }
    }

    // MATH

    public void visitAdd(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitAdd(type, op1, op2, result);
        }
    }

    public void visitSub(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitSub(type, op1, op2, result);
        }
    }

    public void visitMul(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitMul(type, op1, op2, result);
        }
    }

    public void visitDiv(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitDiv(type, op1, op2, result);
        }
    }

    public void visitMod(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitMod(type, op1, op2, result);
        }
    }

    public void visitAnd(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitAnd(type, op1, op2, result);
        }
    }

    public void visitOr(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitOr(type, op1, op2, result);
        }
    }

    public void visitXor(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitXor(type, op1, op2, result);
        }
    }

    public void visitShl(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitShl(type, op1, op2, result);
        }
    }

    public void visitShr(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitShr(type, op1, op2, result);
        }
    }

    public void visitUShr(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitUShr(type, op1, op2, result);
        }
    }

    // LITERAL MATH

    public void visitLitAdd(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitAdd(op1, op2, result);
        }
    }

    public void visitLitRSub(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitRSub(op1, op2, result);
        }
    }

    public void visitLitMul(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitMul(op1, op2, result);
        }
    }

    public void visitLitDiv(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitDiv(op1, op2, result);
        }
    }

    public void visitLitMod(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitMod(op1, op2, result);
        }
    }

    public void visitLitAnd(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitAnd(op1, op2, result);
        }
    }

    public void visitLitOr(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitOr(op1, op2, result);
        }
    }

    public void visitLitXor(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitXor(op1, op2, result);
        }
    }

    public void visitLitShl(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitShl(op1, op2, result);
        }
    }

    public void visitLitShr(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitShr(op1, op2, result);
        }
    }

    public void visitLitUShr(R op1, int op2, R result) {
        if (iv != null) {
            iv.visitLitUShr(op1, op2, result);
        }
    }

    // Math-like instructions

    public void visitNeg(PrimitiveType type, R value, R result) {
        if (iv != null) {
            iv.visitNeg(type, value, result);
        }
    }

    public void visitNot(PrimitiveType type, R value, R result) {
        if (iv != null) {
            iv.visitNot(type, value, result);
        }
    }

    public void visitCmp(R op1, R op2, R result) {
        if (iv != null) {
            iv.visitCmp(op1, op2, result);
        }
    }

    public void visitCmpl(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitCmpl(type, op1, op2, result);
        }
    }

    public void visitCmpg(PrimitiveType type, R op1, R op2, R result) {
        if (iv != null) {
            iv.visitCmpg(type, op1, op2, result);
        }
    }

    // ARRAY RELATED INSTRUCTIONS

    public void visitArrayLength(R array, R result) {
        if (iv != null) {
            iv.visitArrayLength(array, result);
        }
    }

    public void visitArrayLoad(DetailedDexType type, R array, R index, R result) {
        if (iv != null) {
            iv.visitArrayLoad(type, array, index, result);
        }
    }

    public void visitArrayStore(DetailedDexType type, R array, R index, R value) {
        if (iv != null) {
            iv.visitArrayStore(type, array, index, value);
        }
    }

    public void visitFillArray(R array, List<DexNumberConstant> values) {
        if (iv != null) {
            iv.visitFillArray(array, values);
        }
    }

    public void visitNewFilledArray(ArrayType type, List<R> registers) {
        if (iv != null) {
            iv.visitNewFilledArray(type, registers);
        }
    }

    // CAST INSTRUCTIONS

    public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, R fromRegister, R toRegister) {
        if (iv != null) {
            iv.visitPrimitiveCast(fromType, toType, fromRegister, toRegister);
        }
    }

    public void visitRefCast(RefType type, R register) {
        if (iv != null) {
            iv.visitRefCast(type, register);
        }
    }

    // MONITOR INSTRUCTIONS

    public void visitMonitorEnterInstruction(R value) {
        if (iv != null) {
            iv.visitMonitorEnterInstruction(value);
        }
    }

    public void visitMonitorExitInstruction(R value) {
        if (iv != null) {
            iv.visitMonitorExitInstruction(value);
        }
    }

    // CREATE/INSTANCEOF INSTRUCTIONS

    public void visitNew(Path type, R result) {
        if (iv != null) {
            iv.visitNew(type, result);
        }
    }

    public void visitInstanceOf(RefType type, R value, R result) {
        if (iv != null) {
            iv.visitInstanceOf(type, value, result);
        }
    }

    // METHOD EXIT INSTRUCTIONS

    public void visitReturn(DexType type, R register) {
        if (iv != null) {
            iv.visitReturn(type, register);
        }
    }

    public void visitReturnVoid() {
        if (iv != null) {
            iv.visitReturnVoid();
        }
    }

    public void visitThrow(R exception) {
        if (iv != null) {
            iv.visitThrow(exception);
        }
    }

    // FIELD ACCESS

    public void visitFieldGet(FieldRef field, Optional<R> instance, R result) {
        if (iv != null) {
            iv.visitFieldGet(field, instance, result);
        }
    }

    public void visitFieldSet(FieldRef field, Optional<R> instance, R value) {
        if (iv != null) {
            iv.visitFieldSet(field, instance, value);
        }
    }

    // METHOD INVOKE

    public void visitInvoke(Invoke invoke) {
        if (iv != null) {
            iv.visitInvoke(invoke);
        }
    }

    // MOVE INSTRUCTIONS

    public void visitMove(DexType type, R from, R to) {
        if (iv != null) {
            iv.visitMove(type, from, to);
        }
    }

    public void visitMoveResult(DexType type, R register) {
        if (iv != null) {
            iv.visitMoveResult(type, register);
        }
    }

    public void visitMoveException(R target) {
        if (iv != null) {
            iv.visitMoveException(target);
        }
    }

    // CFG INSTRUCTIONS

    public void visitGoto(I target) {
        if (iv != null) {
            iv.visitGoto(target);
        }
    }

    public void visitIf(IfInstruction.Comparison comparison, R op1, Optional<R> op2, I target) {
        if (iv != null) {
            iv.visitIf(comparison, op1, op2, target);
        }
    }

    public void visitSwitch(R value, LinkedHashMap<Integer, I> branchTable, I defaultBranch) {
        if (iv != null) {
            iv.visitSwitch(value, branchTable, defaultBranch);
        }
    }
}
