package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;

import java.util.Optional;
import java.util.Set;

/**
 * Mathematical operation that operates on one {@link Register} and an int literal.
 */
public abstract class AbstractBinaryLitMathInstruction implements Instruction {
    /**
     * First operator of the mathematical operation.
     */
    private Register op1;

    /**
     * Second operator of the mathematical operation.
     */
    private short op2;

    /**
     * The resulting value gets stored in this register.
     */
    private Register result;

    public AbstractBinaryLitMathInstruction(Register op1, short op2, Register result) {
        this.op1 = op1;
        this.op2 = op2;
        this.result = result;
    }

    public Register getOp1() {
        return op1;
    }

    public void setOp1(Register op1) {
        this.op1 = op1;
    }

    public RWCell<Register> getOp1Cell() {
        return RWCell.of(this::getOp1, this::setOp1, Register.class);
    }

    public short getOp2() {
        return op2;
    }

    public void setOp2(short op2) {
        this.op2 = op2;
    }

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    public RWCell<Register> getResultCell() {
        return RWCell.of(this::getResult, this::setResult, Register.class);
    }

    @Override
    public Set<Register> getReadRegisters() {
        return Set.of(op1);
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return Set.of(getOp1Cell());
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.of(getResultCell());
    }
}
