package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Compare two numbers from and the store the result in another {@link Register}.
 *
 * If <code>op1 == op2</code>, the result is <tt>0</tt>.
 * If <code>op1 > op2</code>, the result is <tt>1</tt>.
 * If <code>op1 < op2</code>, the result is <tt>-1</tt>.
 */
public abstract class AbstractCompareInstruction implements Instruction {
    /**
     * The first value to be compared
     */
    private Register op1;

    /**
     * The second value to be compared.
     */
    private Register op2;

    /**
     * Store the result of the comparison in this register.
     */
    private Register result;

    public AbstractCompareInstruction(Register op1, Register op2, Register result) {
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

    public Register getOp2() {
        return op2;
    }

    public void setOp2(Register op2) {
        this.op2 = op2;
    }

    public RWCell<Register> getOp2Cell() {
        return RWCell.of(this::getOp2, this::setOp2, Register.class);
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
    public List<Register> getReadRegisters() {
        return List.of(op1, op2);
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of(getOp1Cell(), getOp2Cell());
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
