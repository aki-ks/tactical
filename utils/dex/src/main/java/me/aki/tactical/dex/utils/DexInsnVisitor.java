package me.aki.tactical.dex.utils;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.dex.insn.FillArrayInstruction;
import me.aki.tactical.dex.insn.IfInstruction;
import me.aki.tactical.dex.insn.Instruction;

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

    public DexInsnVisitor() {
        this(null);
    }

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

    public void visitLitAdd(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitAdd(op1, literal, result);
        }
    }

    public void visitLitRSub(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitRSub(op1, literal, result);
        }
    }

    public void visitLitMul(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitMul(op1, literal, result);
        }
    }

    public void visitLitDiv(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitDiv(op1, literal, result);
        }
    }

    public void visitLitMod(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitMod(op1, literal, result);
        }
    }

    public void visitLitAnd(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitAnd(op1, literal, result);
        }
    }

    public void visitLitOr(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitOr(op1, literal, result);
        }
    }

    public void visitLitXor(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitXor(op1, literal, result);
        }
    }

    public void visitLitShl(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitShl(op1, literal, result);
        }
    }

    public void visitLitShr(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitShr(op1, literal, result);
        }
    }

    public void visitLitUShr(R op1, short literal, R result) {
        if (iv != null) {
            iv.visitLitUShr(op1, literal, result);
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

    public void visitArrayLoad(Type type, R array, R index, R result) {
        if (iv != null) {
            iv.visitArrayLoad(type, array, index, result);
        }
    }

    public void visitArrayStore(Type type, R array, R index, R value) {
        if (iv != null) {
            iv.visitArrayStore(type, array, index, value);
        }
    }

    public void visitFillArray(R array, FillArrayInstruction.NumberSize elementSize, List<FillArrayInstruction.NumericConstant> values) {
        if (iv != null) {
            iv.visitFillArray(array, elementSize, values);
        }
    }

    public void visitNewArray(ArrayType type, R size, R result) {
        if (iv != null) {
            iv.visitNewArray(type, size, result);
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

    public void visitMonitorEnter(R value) {
        if (iv != null) {
            iv.visitMonitorEnter(value);
        }
    }

    public void visitMonitorExit(R value) {
        if (iv != null) {
            iv.visitMonitorExit(value);
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

    public void visitReturn(R register) {
        if (iv != null) {
            iv.visitReturn(register);
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

    public static enum InvokeType {
        DIRECT, INTERFACE, STATIC, SUPER, VIRTUAL, POLYMORPHIC;
    }

    public void visitInvoke(InvokeType invoke, MethodRef method, Optional<R> instance, List<R> arguments) {
        if (iv != null) {
            iv.visitInvoke(invoke, method, instance, arguments);
        }
    }

    public void visitCustomInvoke(List<R> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
        if (iv != null) {
            iv.visitCustomInvoke(arguments, name, descriptor, bootstrapArguments, bootstrapMethod);
        }
    }

    // MOVE INSTRUCTIONS

    public void visitMove(Type type, R from, R to) {
        if (iv != null) {
            iv.visitMove(type, from, to);
        }
    }

    public void visitMoveResult(R register) {
        if (iv != null) {
            iv.visitMoveResult(register);
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

    public void visitSwitch(R value, LinkedHashMap<Integer, I> branchTable) {
        if (iv != null) {
            iv.visitSwitch(value, branchTable);
        }
    }
}
