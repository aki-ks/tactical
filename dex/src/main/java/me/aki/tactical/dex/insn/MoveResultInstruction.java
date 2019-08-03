package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Move the returned value of a just invoked method to a {@link Register}.
 *
 * This Instruction is also used to get the result of a {@link NewFilledArrayInstruction}.
 */
public class MoveResultInstruction implements Instruction {
    /**
     * Type of value to be moved.
     */
    private DexType type;

    /**
     * Where should the value get move to.
     */
    private Register register;

    public MoveResultInstruction(DexType type, Register register) {
        this.type = type;
        this.register = register;
    }

    public DexType getType() {
        return type;
    }

    public void setType(DexType type) {
        this.type = type;
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
