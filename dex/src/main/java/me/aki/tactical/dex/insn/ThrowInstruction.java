package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

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
    public List<Register> getReadRegisters() {
        return List.of(register);
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of(getRegisterCell());
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
