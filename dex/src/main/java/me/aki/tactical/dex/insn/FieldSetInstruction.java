package me.aki.tactical.dex.insn;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public List<Register> getReadRegisters() {
        List<Register> registers = new ArrayList<>();
        instance.ifPresent(registers::add);
        registers.add(value);
        return List.copyOf(registers);
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        List<RWCell<Register>> registers = new ArrayList<>();
        getInstanceCell().ifPresent(registers::add);
        registers.add(getValueCell());
        return registers;
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
