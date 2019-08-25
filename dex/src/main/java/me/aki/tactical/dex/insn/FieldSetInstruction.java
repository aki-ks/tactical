package me.aki.tactical.dex.insn;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Store a value in a field
 */
public class FieldSetInstruction implements Instruction {
    /**
     * Store the value in this field
     */
    private FieldRef field;

    /**
     * Instance of the class containing the field or empty for static fields.
     */
    private Optional<Register> instance;

    /**
     * Value to be stored in the field;
     */
    private Register value;

    public FieldSetInstruction(FieldRef field, Optional<Register> instance, Register value) {
        this.field = field;
        this.instance = instance;
        this.value = value;
    }

    public FieldRef getField() {
        return field;
    }

    public void setField(FieldRef field) {
        this.field = field;
    }

    public Optional<Register> getInstance() {
        return instance;
    }

    public void setInstance(Optional<Register> instance) {
        this.instance = instance;
    }

    public Optional<RWCell<Register>> getInstanceCell() {
        return instance.map(x -> RWCell.of(() -> this.instance.get(), instance -> this.instance = Optional.of(instance), Register.class));
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
        return Stream.concat(instance.stream(), Stream.of(value))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return Stream.concat(getInstanceCell().stream(), Stream.of(getValueCell()))
                .collect(Collectors.toUnmodifiableSet());
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
