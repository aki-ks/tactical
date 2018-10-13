package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.DetailedDexType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Get one value contained within an array
 */
public class ArrayLoadStatement implements Statement {
    /**
     * Type of element to get.
     */
    private DetailedDexType type;

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

    public ArrayLoadStatement(DetailedDexType type, Register array, Register index, Register result) {
        this.type = type;
        this.array = array;
        this.index = index;
        this.result = result;
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

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(array, index);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }
}
