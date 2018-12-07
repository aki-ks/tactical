package me.aki.tactical.dex.insn;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Get the value of a field.
 */
public class FieldGetInstruction implements Instruction {
    /**
     * The field whose value is requested.
     */
    private FieldRef field;

    /**
     * Instance of the class containing the field or empty for static fields.
     */
    private Optional<Register> instance;

    /**
     * Store the value of the field in this register
     */
    private Register result;

    public FieldGetInstruction(FieldRef field, Optional<Register> instance, Register result) {
        this.field = field;
        this.instance = instance;
        this.result = result;
    }

    public boolean isStatic() {
        return !instance.isPresent();
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

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    @Override
    public List<Register> getReadRegisters() {
        return instance.map(List::of).orElseGet(List::of);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }
}
