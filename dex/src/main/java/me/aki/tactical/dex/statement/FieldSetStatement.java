package me.aki.tactical.dex.statement;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.dex.Register;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Store a value in a field
 */
public class FieldSetStatement implements Statement {
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

    public FieldSetStatement(FieldRef field, Optional<Register> instance, Register value) {
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

    public Register getValue() {
        return value;
    }

    public void setValue(Register value) {
        this.value = value;
    }

    @Override
    public List<Register> getReadRegisters() {
        List<Register> registers = new ArrayList<>();
        instance.ifPresent(registers::add);
        registers.add(value);
        return List.copyOf(registers);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }
}
