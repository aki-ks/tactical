package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.Optional;
import java.util.Set;

/**
 * Store an element at an index within an array.
 */
public class ArrayStoreInstruction implements Instruction {
    /**
     * Type of the value to be stored in the array.
     *
     * It should be either one of the {@link PrimitiveType PrimitiveType} singletons or
     * an instance of a {@link RefType} such as {@link ObjectType#OBJECT}.
     */
    private Type type;

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

    public ArrayStoreInstruction(Type type, Register array, Register index, Register value) {
        this.type = type;
        this.array = array;
        this.index = index;
        this.value = value;
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
    public Set<Register> getReadRegisters() {
        return Set.of(array, index, value);
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return Set.of(getArrayCell(), getIndexCell(), getValueCell());
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
