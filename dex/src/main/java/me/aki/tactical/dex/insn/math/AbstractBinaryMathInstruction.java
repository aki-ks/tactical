package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;

import java.util.List;
import java.util.Optional;

/**
 * Math operation that operates on two registers and stores the result in another register.
 */
public abstract class AbstractBinaryMathInstruction implements Instruction {
    /**
     * Type of values that this instruction operates on.
     */
    private PrimitiveType type;

    /**
     * First operator of the mathematical operation.
     */
    private Register op1;

    /**
     * Second operator of the mathematical operation.
     */
    private Register op2;

    /**
     * The result of the operation gets stored in this register.
     */
    private Register result;

    public AbstractBinaryMathInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        this.op1 = op1;
        this.op2 = op2;
        this.result = result;
        setType(type);
    }

    public PrimitiveType getType() {
        return type;
    }

    public void setType(PrimitiveType type) {
        if (isTypeSupported(type)) {
            this.type = type;
        } else {
            throw new IllegalStateException("Type " + type.getClass().getSimpleName() + " is not supported by " + this.getClass().getSimpleName());
        }
    }

    public Register getOp1() {
        return op1;
    }

    public void setOp1(Register op1) {
        this.op1 = op1;
    }

    public Register getOp2() {
        return op2;
    }

    public void setOp2(Register op2) {
        this.op2 = op2;
    }

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    protected abstract boolean isTypeSupported(PrimitiveType type);

    @Override
    public List<Register> getReadRegisters() {
        return List.of(op1, op2);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(op1);
    }
}
