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
import me.aki.tactical.dex.DetailedDexType;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.insn.FillArrayInstruction;
import me.aki.tactical.dex.insn.IfInstruction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class DebugDexInsnVisitor<I, R> extends DexInsnVisitor<I, R> {
    public DebugDexInsnVisitor() {
        this(null);
    }

    public DebugDexInsnVisitor(DexInsnVisitor<I, R> iv) {
        super(iv);
    }

    @Override
    public void visitConstant(DexConstant constant, R target) {
        super.visitConstant(constant, target);
        System.out.println("visitConstant(constant = " + constant + ", target = " + target + ")");
    }

    @Override
    public void visitAdd(PrimitiveType type, R op1, R op2, R result) {
        super.visitAdd(type, op1, op2, result);
        System.out.println("visitAdd(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitSub(PrimitiveType type, R op1, R op2, R result) {
        super.visitSub(type, op1, op2, result);
        System.out.println("visitSub(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitMul(PrimitiveType type, R op1, R op2, R result) {
        super.visitMul(type, op1, op2, result);
        System.out.println("visitMul(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitDiv(PrimitiveType type, R op1, R op2, R result) {
        super.visitDiv(type, op1, op2, result);
        System.out.println("visitDiv(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitMod(PrimitiveType type, R op1, R op2, R result) {
        super.visitMod(type, op1, op2, result);
        System.out.println("visitMod(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitAnd(PrimitiveType type, R op1, R op2, R result) {
        super.visitAnd(type, op1, op2, result);
        System.out.println("visitAnd(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitOr(PrimitiveType type, R op1, R op2, R result) {
        super.visitOr(type, op1, op2, result);
        System.out.println("visitOr(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitXor(PrimitiveType type, R op1, R op2, R result) {
        super.visitXor(type, op1, op2, result);
        System.out.println("visitXor(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitShl(PrimitiveType type, R op1, R op2, R result) {
        super.visitShl(type, op1, op2, result);
        System.out.println("visitShl(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitShr(PrimitiveType type, R op1, R op2, R result) {
        super.visitShr(type, op1, op2, result);
        System.out.println("visitShr(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitUShr(PrimitiveType type, R op1, R op2, R result) {
        super.visitUShr(type, op1, op2, result);
        System.out.println("visitUShr(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitLitAdd(R op1, short literal, R result) {
        super.visitLitAdd(op1, literal, result);
        System.out.println("visitLitAdd(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitRSub(R op1, short literal, R result) {
        super.visitLitRSub(op1, literal, result);
        System.out.println("visitLitRSub(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitMul(R op1, short literal, R result) {
        super.visitLitMul(op1, literal, result);
        System.out.println("visitLitMul(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitDiv(R op1, short literal, R result) {
        super.visitLitDiv(op1, literal, result);
        System.out.println("visitLitDiv(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitMod(R op1, short literal, R result) {
        super.visitLitMod(op1, literal, result);
        System.out.println("visitLitMod(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitAnd(R op1, short literal, R result) {
        super.visitLitAnd(op1, literal, result);
        System.out.println("visitLitAnd(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitOr(R op1, short literal, R result) {
        super.visitLitOr(op1, literal, result);
        System.out.println("visitLitOr(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitXor(R op1, short literal, R result) {
        super.visitLitXor(op1, literal, result);
        System.out.println("visitLitXor(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitShl(R op1, short literal, R result) {
        super.visitLitShl(op1, literal, result);
        System.out.println("visitLitShl(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitShr(R op1, short literal, R result) {
        super.visitLitShr(op1, literal, result);
        System.out.println("visitLitShr(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitLitUShr(R op1, short literal, R result) {
        super.visitLitUShr(op1, literal, result);
        System.out.println("visitLitUShr(op1 = " + op1 + ", literal = " + literal + ", result = " + result + ")");
    }

    @Override
    public void visitNeg(PrimitiveType type, R value, R result) {
        super.visitNeg(type, value, result);
        System.out.println("visitNeg(type = " + type + ", value = " + value + ", result = " + result + ")");
    }

    @Override
    public void visitNot(PrimitiveType type, R value, R result) {
        super.visitNot(type, value, result);
        System.out.println("visitNot(type = " + type + ", value = " + value + ", result = " + result + ")");
    }

    @Override
    public void visitCmp(R op1, R op2, R result) {
        super.visitCmp(op1, op2, result);
        System.out.println("visitCmp(op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitCmpl(PrimitiveType type, R op1, R op2, R result) {
        super.visitCmpl(type, op1, op2, result);
        System.out.println("visitCmpl(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitCmpg(PrimitiveType type, R op1, R op2, R result) {
        super.visitCmpg(type, op1, op2, result);
        System.out.println("visitCmpg(type = " + type + ", op1 = " + op1 + ", op2 = " + op2 + ", result = " + result + ")");
    }

    @Override
    public void visitArrayLength(R array, R result) {
        super.visitArrayLength(array, result);
        System.out.println("visitArrayLength(array = " + array + ", result = " + result + ")");
    }

    @Override
    public void visitArrayLoad(DetailedDexType type, R array, R index, R result) {
        super.visitArrayLoad(type, array, index, result);
        System.out.println("visitArrayLoad(type = " + type + ", array = " + array + ", index = " + index + ", result = " + result + ")");
    }

    @Override
    public void visitArrayStore(DetailedDexType type, R array, R index, R value) {
        super.visitArrayStore(type, array, index, value);
        System.out.println("visitArrayStore(type = " + type + ", array = " + array + ", index = " + index + ", value = " + value + ")");
    }

    @Override
    public void visitFillArray(R array, FillArrayInstruction.NumberSize elementSize, List<FillArrayInstruction.NumericConstant> values) {
        super.visitFillArray(array, elementSize, values);
        System.out.println("visitFillArray(array = " + array + ", elementSize = " + elementSize + ", values = " + values + ")");
    }

    @Override
    public void visitNewArray(ArrayType type, R size, R result) {
        super.visitNewArray(type, size, result);
        System.out.println("visitNewArray(type = " + type + ", size = " + size + ", result = " + result + ")");
    }

    @Override
    public void visitNewFilledArray(ArrayType type, List<R> registers) {
        super.visitNewFilledArray(type, registers);
        System.out.println("visitNewFilledArray(type = " + type + ", registers = " + registers + ")");
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, R fromRegister, R toRegister) {
        super.visitPrimitiveCast(fromType, toType, fromRegister, toRegister);
        System.out.println("visitPrimitiveCast(fromType = " + fromType + ", toType = " + toType + ", fromRegister = " + fromRegister + ", toRegister = " + toRegister + ")");
    }

    @Override
    public void visitRefCast(RefType type, R register) {
        super.visitRefCast(type, register);
        System.out.println("visitRefCast(type = " + type + ", register = " + register + ")");
    }

    @Override
    public void visitMonitorEnter(R value) {
        super.visitMonitorEnter(value);
        System.out.println("visitMonitorEnter(value = " + value + ")");
    }

    @Override
    public void visitMonitorExit(R value) {
        super.visitMonitorExit(value);
        System.out.println("visitMonitorExit(value = " + value + ")");
    }

    @Override
    public void visitNew(Path type, R result) {
        super.visitNew(type, result);
        System.out.println("visitNew(type = " + type + ", result = " + result + ")");
    }

    @Override
    public void visitInstanceOf(RefType type, R value, R result) {
        super.visitInstanceOf(type, value, result);
        System.out.println("visitInstanceOf(type = " + type + ", value = " + value + ", result = " + result + ")");
    }

    @Override
    public void visitReturn(DexType type, R register) {
        super.visitReturn(type, register);
        System.out.println("visitReturn(type = " + type + ", register = " + register + ")");
    }

    @Override
    public void visitReturnVoid() {
        super.visitReturnVoid();
        System.out.println("visitReturnVoid()");
    }

    @Override
    public void visitThrow(R exception) {
        super.visitThrow(exception);
        System.out.println("visitThrow(exception = " + exception + ")");
    }

    @Override
    public void visitFieldGet(FieldRef field, Optional<R> instance, R result) {
        super.visitFieldGet(field, instance, result);
        System.out.println("visitFieldGet(field = " + field + ", instance = " + instance + ", result = " + result + ")");
    }

    @Override
    public void visitFieldSet(FieldRef field, Optional<R> instance, R value) {
        super.visitFieldSet(field, instance, value);
        System.out.println("visitFieldSet(field = " + field + ", instance = " + instance + ", value = " + value + ")");
    }

    @Override
    public void visitInvoke(InvokeType invoke, MethodRef method, Optional<R> instance, List<R> arguments) {
        super.visitInvoke(invoke, method, instance, arguments);
        System.out.println("visitInvoke(invoke = " + invoke + ", method = " + method + ", instance = " + instance + ", arguments = " + arguments + ")");
    }

    @Override
    public void visitCustomInvoke(List<R> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
        super.visitCustomInvoke(arguments, name, descriptor, bootstrapArguments, bootstrapMethod);
        System.out.println("visitCustomInvoke(arguments = " + arguments + ", name = " + name + ", descriptor = " + descriptor + ", bootstrapArguments = " + bootstrapArguments + ", bootstrapMethod = " + bootstrapMethod + ")");
    }

    @Override
    public void visitMove(DexType type, R from, R to) {
        super.visitMove(type, from, to);
        System.out.println("visitMove(type = " + type + ", from = " + from + ", to = " + to + ")");
    }

    @Override
    public void visitMoveResult(DexType type, R register) {
        super.visitMoveResult(type, register);
        System.out.println("visitMoveResult(type = " + type + ", register = " + register + ")");
    }

    @Override
    public void visitMoveException(R target) {
        super.visitMoveException(target);
        System.out.println("visitMoveException(target = " + target + ")");
    }

    @Override
    public void visitGoto(I target) {
        super.visitGoto(target);
        System.out.println("visitGoto(target = " + target + ")");
    }

    @Override
    public void visitIf(IfInstruction.Comparison comparison, R op1, Optional<R> op2, I target) {
        super.visitIf(comparison, op1, op2, target);
        System.out.println("visitIf(comparison = " + comparison + ", op1 = " + op1 + ", op2 = " + op2 + ", target = " + target + ")");
    }

    @Override
    public void visitSwitch(R value, LinkedHashMap<Integer, I> branchTable) {
        super.visitSwitch(value, branchTable);
        System.out.println("visitSwitch(value = " + value + ", branchTable = " + branchTable + ")");
    }
}
