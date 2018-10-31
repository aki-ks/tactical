package me.aki.tactical.dex.statement.litmath;

import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.statement.Statement;

import java.util.List;
import java.util.Optional;

/**
 * Mathematical operation that operates on one {@link Register} and an int literal.
 */
public abstract class AbstractBinaryLitMathStatement implements Statement {
    /**
     * First operator of the mathematical operation.
     */
    private Register op1;

    /**
     * Second operator of the mathematical operation.
     */
    private int op2;

    /**
     * The resulting value gets stored in this register.
     */
    private Register result;

    public AbstractBinaryLitMathStatement(Register op1, int op2, Register result) {
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

    public int getOp2() {
        return op2;
    }

    public void setOp2(int op2) {
        this.op2 = op2;
    }

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(op1);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }
}
