package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Calculate the bitwise complement of an int or long.
 */
public class NotInstruction implements Instruction {
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

    public NotInstruction(PrimitiveType type, Register value, Register result) {
        setType(type);
        this.value = value;
        this.result = result;
    }

    public PrimitiveType getType() {
        return type;
    }

    public void setType(PrimitiveType type) {
        if (!(type instanceof IntLikeType) || !(type instanceof LongType)) {
            throw new IllegalArgumentException("Only values of int or long type can be bitwise negated");
        }

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
