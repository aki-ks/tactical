package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.*;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Get one value contained within an array
 */
public class ArrayLoadInstruction implements Instruction {
    /**
     * Type of the value to be read from the array.
     *
     * It should be either one of the {@link PrimitiveType PrimitiveType} singletons or
     * an instance of a {@link RefType} such as {@link ObjectType#OBJECT}.
     */
    private Type type;

    /**
     * Register that contains the array to read from.
     */
    private Register array;

    /**
     * Register with the index of the element within the array.
     */
    private Register index;

    /**
     * Store the retrieved value within that local.
     */
    private Register result;

    public ArrayLoadInstruction(Type type, Register array, Register index, Register result) {
        this.type = type;
        this.array = array;
        this.index = index;
        this.result = result;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public Register getIndex() {
        return index;
    }

    public void setIndex(Register index) {
        this.index = index;
    }

    public RWCell<Register> getIndexCell() {
        return RWCell.of(this::getIndex, this::setIndex, Register.class);
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
        return List.of(array, index);
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of(getArrayCell(), getIndexCell());
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
