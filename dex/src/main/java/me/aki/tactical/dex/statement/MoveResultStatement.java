package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Move the returned value of a just invoked method to a {@link Register}.
 */
public class MoveResultStatement implements Statement {
    /**
     * Type of value to be moved.
     */
    private DexType type;

    /**
     * Where should the value get move to.
     */
    private Register register;

    public MoveResultStatement(DexType type, Register register) {
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

    @Override
    public List<Register> getReadRegisters() {
        return List.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(register);
    }
}
