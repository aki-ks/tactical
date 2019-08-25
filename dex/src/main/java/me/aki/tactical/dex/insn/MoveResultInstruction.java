package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.Optional;
import java.util.Set;

/**
 * Move the returned value of a just invoked method to a {@link Register}.
 *
 * This Instruction is also used to get the result of a {@link NewFilledArrayInstruction}.
 */
public class MoveResultInstruction implements Instruction {
    /**
     * Where should the value get move to.
     */
    private Register register;

    public MoveResultInstruction(Register register) {
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
        return Set.of();
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return Set.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(register);
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.of(getRegisterCell());
    }
}
