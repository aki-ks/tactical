package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Move a caught exception to a {@link Register}.
 */
public class MoveExceptionInstruction implements Instruction {
    /**
     * Where should the exception get stored.
     */
    private Register register;

    public MoveExceptionInstruction(Register register) {
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
        return List.of();
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of();
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
