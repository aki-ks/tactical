package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.core.constant.DexNumberConstant;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.type.*;
import me.aki.tactical.dex.DetailedDexType;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.insn.IfInstruction;
import me.aki.tactical.dex.utils.DexInsnVisitor;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction12x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction23x;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class SmaliInsnWriter extends DexInsnVisitor<Instruction, Integer> {
    private List<Instruction> instructions = new ArrayList<>();

    private void visitInstruction(Instruction insn) {
        instructions.add(insn);
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public List<Instruction> popInstructions() {
        List<Instruction> instructions = this.instructions;
        this.instructions = new ArrayList<>();
        return instructions;
    }

    @Override
    public void visitConstant(DexConstant constant, Integer target) {
        super.visitConstant(constant, target);
    }

    // MATH //

    @Override
    public void visitAdd(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.ADD_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.ADD_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.ADD_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.ADD_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.ADD_INT; }
                public Opcode caseLong() { return Opcode.ADD_LONG; }
                public Opcode caseFloat() { return Opcode.ADD_FLOAT; }
                public Opcode caseDouble() { return Opcode.ADD_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitSub(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SUB_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.SUB_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.SUB_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.SUB_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SUB_INT; }
                public Opcode caseLong() { return Opcode.SUB_LONG; }
                public Opcode caseFloat() { return Opcode.SUB_FLOAT; }
                public Opcode caseDouble() { return Opcode.SUB_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitMul(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.MUL_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.MUL_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.MUL_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.MUL_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.MUL_INT; }
                public Opcode caseLong() { return Opcode.MUL_LONG; }
                public Opcode caseFloat() { return Opcode.MUL_FLOAT; }
                public Opcode caseDouble() { return Opcode.MUL_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitDiv(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.DIV_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.DIV_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.DIV_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.DIV_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.DIV_INT; }
                public Opcode caseLong() { return Opcode.DIV_LONG; }
                public Opcode caseFloat() { return Opcode.DIV_FLOAT; }
                public Opcode caseDouble() { return Opcode.DIV_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitMod(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.REM_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.REM_LONG_2ADDR; }
                public Opcode caseFloat() { return Opcode.REM_FLOAT_2ADDR; }
                public Opcode caseDouble() { return Opcode.REM_DOUBLE_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILFDTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.REM_INT; }
                public Opcode caseLong() { return Opcode.REM_LONG; }
                public Opcode caseFloat() { return Opcode.REM_FLOAT; }
                public Opcode caseDouble() { return Opcode.REM_DOUBLE; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitAnd(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.AND_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.AND_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.AND_INT; }
                public Opcode caseLong() { return Opcode.AND_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitOr(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.OR_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.OR_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.OR_INT; }
                public Opcode caseLong() { return Opcode.OR_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitXor(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.XOR_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.XOR_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.XOR_INT; }
                public Opcode caseLong() { return Opcode.XOR_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitShl(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SHL_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.SHL_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SHL_INT; }
                public Opcode caseLong() { return Opcode.SHL_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitShr(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SHR_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.SHR_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.SHR_INT; }
                public Opcode caseLong() { return Opcode.SHR_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitUShr(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        if (op1.equals(result)) {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.USHR_INT_2ADDR; }
                public Opcode caseLong() { return Opcode.USHR_LONG_2ADDR; }
            });

            visitInstruction(new ImmutableInstruction12x(opcode, op1, op2));
        } else {
            Opcode opcode = match(type, new ILTypeMatch<>() {
                public Opcode caseIntLike(IntLikeType type) { return Opcode.USHR_INT; }
                public Opcode caseLong() { return Opcode.USHR_LONG; }
            });

            visitInstruction(new ImmutableInstruction23x(opcode, result, op1, op2));
        }
    }

    @Override
    public void visitLitAdd(Integer op1, int op2, Integer result) {
        super.visitLitAdd(op1, op2, result);
    }

    @Override
    public void visitLitRSub(Integer op1, int op2, Integer result) {
        super.visitLitRSub(op1, op2, result);
    }

    @Override
    public void visitLitMul(Integer op1, int op2, Integer result) {
        super.visitLitMul(op1, op2, result);
    }

    @Override
    public void visitLitDiv(Integer op1, int op2, Integer result) {
        super.visitLitDiv(op1, op2, result);
    }

    @Override
    public void visitLitMod(Integer op1, int op2, Integer result) {
        super.visitLitMod(op1, op2, result);
    }

    @Override
    public void visitLitAnd(Integer op1, int op2, Integer result) {
        super.visitLitAnd(op1, op2, result);
    }

    @Override
    public void visitLitOr(Integer op1, int op2, Integer result) {
        super.visitLitOr(op1, op2, result);
    }

    @Override
    public void visitLitXor(Integer op1, int op2, Integer result) {
        super.visitLitXor(op1, op2, result);
    }

    @Override
    public void visitLitShl(Integer op1, int op2, Integer result) {
        super.visitLitShl(op1, op2, result);
    }

    @Override
    public void visitLitShr(Integer op1, int op2, Integer result) {
        super.visitLitShr(op1, op2, result);
    }

    @Override
    public void visitLitUShr(Integer op1, int op2, Integer result) {
        super.visitLitUShr(op1, op2, result);
    }

    @Override
    public void visitNeg(PrimitiveType type, Integer value, Integer result) {
        super.visitNeg(type, value, result);
    }

    @Override
    public void visitNot(PrimitiveType type, Integer value, Integer result) {
        super.visitNot(type, value, result);
    }

    @Override
    public void visitCmp(Integer op1, Integer op2, Integer result) {
        super.visitCmp(op1, op2, result);
    }

    @Override
    public void visitCmpl(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        super.visitCmpl(type, op1, op2, result);
    }

    @Override
    public void visitCmpg(PrimitiveType type, Integer op1, Integer op2, Integer result) {
        super.visitCmpg(type, op1, op2, result);
    }

    @Override
    public void visitArrayLength(Integer array, Integer result) {
        super.visitArrayLength(array, result);
    }

    @Override
    public void visitArrayLoad(DetailedDexType type, Integer array, Integer index, Integer result) {
        super.visitArrayLoad(type, array, index, result);
    }

    @Override
    public void visitArrayStore(DetailedDexType type, Integer array, Integer index, Integer value) {
        super.visitArrayStore(type, array, index, value);
    }

    @Override
    public void visitFillArray(Integer array, List<DexNumberConstant> values) {
        super.visitFillArray(array, values);
    }

    @Override
    public void visitNewArray(ArrayType type, Integer size, Integer result) {
        super.visitNewArray(type, size, result);
    }

    @Override
    public void visitNewFilledArray(ArrayType type, List<Integer> registers) {
        super.visitNewFilledArray(type, registers);
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, Integer fromRegister, Integer toRegister) {
        super.visitPrimitiveCast(fromType, toType, fromRegister, toRegister);
    }

    @Override
    public void visitRefCast(RefType type, Integer register) {
        super.visitRefCast(type, register);
    }

    @Override
    public void visitMonitorEnter(Integer value) {
        super.visitMonitorEnter(value);
    }

    @Override
    public void visitMonitorExit(Integer value) {
        super.visitMonitorExit(value);
    }

    @Override
    public void visitNew(Path type, Integer result) {
        super.visitNew(type, result);
    }

    @Override
    public void visitInstanceOf(RefType type, Integer value, Integer result) {
        super.visitInstanceOf(type, value, result);
    }

    @Override
    public void visitReturn(DexType type, Integer register) {
        super.visitReturn(type, register);
    }

    @Override
    public void visitReturnVoid() {
        super.visitReturnVoid();
    }

    @Override
    public void visitThrow(Integer exception) {
        super.visitThrow(exception);
    }

    @Override
    public void visitFieldGet(FieldRef field, Optional<Integer> instance, Integer result) {
        super.visitFieldGet(field, instance, result);
    }

    @Override
    public void visitFieldSet(FieldRef field, Optional<Integer> instance, Integer value) {
        super.visitFieldSet(field, instance, value);
    }

    @Override
    public void visitInvoke(InvokeType invoke, MethodRef method, Optional<Integer> instance, List<Integer> arguments) {
        super.visitInvoke(invoke, method, instance, arguments);
    }

    @Override
    public void visitCustomInvoke(List<Integer> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
        super.visitCustomInvoke(arguments, name, descriptor, bootstrapArguments, bootstrapMethod);
    }

    @Override
    public void visitMove(DexType type, Integer from, Integer to) {
        super.visitMove(type, from, to);
    }

    @Override
    public void visitMoveResult(DexType type, Integer register) {
        super.visitMoveResult(type, register);
    }

    @Override
    public void visitMoveException(Integer target) {
        super.visitMoveException(target);
    }

    @Override
    public void visitGoto(Instruction target) {
        super.visitGoto(target);
    }

    @Override
    public void visitIf(IfInstruction.Comparison comparison, Integer op1, Optional<Integer> op2, Instruction target) {
        super.visitIf(comparison, op1, op2, target);
    }

    @Override
    public void visitSwitch(Integer value, LinkedHashMap<Integer, Instruction> branchTable) {
        super.visitSwitch(value, branchTable);
    }

    // UTILS //

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
            throw new AssertionError();
        }
    }

    private <T> T match(PrimitiveType type, ILTypeMatch<T> matcher) {
        if (type instanceof IntLikeType) {
            return matcher.caseIntLike((IntLikeType) type);
        } else if (type instanceof LongType) {
            return matcher.caseLong();
        } else {
            throw new AssertionError();
        }
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
}
