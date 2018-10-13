package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Acquire a lock on a value within a {@link Register}.
 */
public class MonitorEnterStatement implements Statement {
    /**
     * Acquire a lock on the value contained in that register.
     */
    private Register register;

    public MonitorEnterStatement(Register register) {
        this.register = register;
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
        return Optional.empty();
    }
}
