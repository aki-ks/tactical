package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.Optional;
import java.util.Set;

/**
 * Throw an exception contained within a certain {@link Register}.
 */
public class ThrowInstruction implements Instruction {
    /**
     * Register that contains the exception.
     */
    private Register register;

    public ThrowInstruction(Register register) {
        this.register = register;
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public RWCell<Register> getRegisterCell() {
        return RWCell.of(this::getRegister, this::setRegister, Register.class);
    }

    @Override
    public Set<Register> getReadRegisters() {
        return Set.of(register);
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return Set.of(getRegisterCell());
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.empty();
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }
}
