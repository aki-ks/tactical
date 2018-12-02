package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Get the length of an array.
 *
 * The length of an array within one {@link Register} gets written to another {@link Register}.
 */
public class ArrayLengthStatement implements Statement {
    /**
     * Register that contains the array whose length is request.
     */
    private Register array;

    /**
     * The length of the array is stored in this register.
     */
    private Register result;

    public ArrayLengthStatement(Register array, Register result) {
        this.array = array;
        this.result = result;
    }

    public Register getArray() {
        return array;
    }

    public void setArray(Register array) {
        this.array = array;
    }

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(array);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }
}
