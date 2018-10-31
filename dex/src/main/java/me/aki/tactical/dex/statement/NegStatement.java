package me.aki.tactical.dex.statement;

import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Negate a numeric value.
 */
public class NegStatement implements Statement {
    /**
     * Type of number to be negated.
     */
    private PrimitiveType type;

    /**
     * Register that contains the value to be negated.
     */
    private Register value;

    /**
     * Store the negated value in this register
     */
    private Register result;

    public NegStatement(PrimitiveType type, Register value, Register result) {
        this.type = type;
        this.value = value;
        this.result = result;
    }

    public PrimitiveType getType() {
        return type;
    }

    public void setType(PrimitiveType type) {
        this.type = type;
    }

    public Register getValue() {
        return value;
    }

    public void setValue(Register value) {
        this.value = value;
    }

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(value);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }
}
