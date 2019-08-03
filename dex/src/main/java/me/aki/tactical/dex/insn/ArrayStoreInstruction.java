package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Store an element at an index within an array.
 */
public class ArrayStoreInstruction implements Instruction {
    /**
     * Register that contains the array to store the element in.
     */
    private Register array;

    /**
     * Register with the index at which the element gets stored.
     */
    private Register index;

    /**
     * Register with the value that gets stored within the array.
     */
    private Register value;

    public ArrayStoreInstruction(Register array, Register index, Register value) {
        this.array = array;
        this.index = index;
        this.value = value;
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

    public Register getValue() {
        return value;
    }

    public void setValue(Register value) {
        this.value = value;
    }

    public RWCell<Register> getValueCell() {
        return RWCell.of(this::getValue, this::setValue, Register.class);
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(array, index, value);
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of(getArrayCell(), getIndexCell(), getValueCell());
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.empty();
    }
}
