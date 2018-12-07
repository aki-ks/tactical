package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.RefType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Ensure that an object value within a {@link Register} is of a certain type.
 */
public class RefCastInstruction implements Instruction {
    /**
     * Check whether the value is of this type
     */
    private RefType type;

    /**
     * Register whose value is checked.
     */
    private Register register;

    public RefType getType() {
        return type;
    }

    public void setType(RefType type) {
        this.type = type;
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(register);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        // This instruction only reads from the register and throws an exception if the value
        // is not of the expected type. It will not alter the value within the register.
        return Optional.empty();
    }
}
