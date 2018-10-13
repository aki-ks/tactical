package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.DetailedDexType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Store an element at an index within an array.
 */
public class ArrayStoreStatement implements Statement {
    /**
     * Type of element to store within the array.
     */
    private DetailedDexType type;

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

    public ArrayStoreStatement(DetailedDexType type, Register array, Register index, Register value) {
        this.type = type;
        this.array = array;
        this.index = index;
        this.value = value;
    }

    public DetailedDexType getType() {
        return type;
    }

    public void setType(DetailedDexType type) {
        this.type = type;
    }

    public Register getArray() {
        return array;
    }

    public void setArray(Register array) {
        this.array = array;
    }

    public Register getIndex() {
        return index;
    }

    public void setIndex(Register index) {
        this.index = index;
    }

    public Register getValue() {
        return value;
    }

    public void setValue(Register value) {
        this.value = value;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(array, index, value);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }
}
