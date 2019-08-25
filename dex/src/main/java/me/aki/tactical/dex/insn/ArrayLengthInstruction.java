package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.Optional;
import java.util.Set;

/**
 * Get the length of an array.
 *
 * The length of an array within one {@link Register} gets written to another {@link Register}.
 */
public class ArrayLengthInstruction implements Instruction {
    /**
     * Register that contains the array whose length is request.
     */
    private Register array;

    /**
     * The length of the array is stored in this register.
     */
    private Register result;

    public ArrayLengthInstruction(Register array, Register result) {
        this.array = array;
        this.result = result;
    }

    public Register getArray() {
        return array;
    }

    public void setArray(Register array) {
        this.array = array;
    }

    public RWCell<Register> getArrayCell() {
        return RWCell.of(this::getArray, this::setArray, Register.class);
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
        return Set.of(array);
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return Set.of(getArrayCell());
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
