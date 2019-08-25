package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Create an array of a certain type of a runtime defined size.
 */
public class NewArrayInstruction implements Instruction {
    /**
     * The type of array that gets created.
     */
    private ArrayType arrayType;

    /**
     * Register containing the size of the array.
     */
    private Register size;

    /**
     * Write the created array into this register.
     */
    private Register result;

    public NewArrayInstruction(ArrayType arrayType, Register size, Register result) {
        this.arrayType = arrayType;
        this.size = size;
        this.result = result;
    }

    public ArrayType getArrayType() {
        return arrayType;
    }

    public void setArrayType(ArrayType arrayType) {
        this.arrayType = arrayType;
    }

    public Register getSize() {
        return size;
    }

    public void setSize(Register size) {
        this.size = size;
    }

    public RWCell<Register> getSizeCell() {
        return RWCell.of(this::getSize, this::setSize, Register.class);
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
        return Set.of(size);
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return Set.of(getSizeCell());
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
