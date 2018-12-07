package me.aki.tactical.dex.insn;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Release a lock from a value within a {@link Register}.
 */
public class MonitorExitInstruction implements Instruction {
    /**
     * Release a lock from the value contained in that register.
     */
    private Register register;

    public MonitorExitInstruction(Register register) {
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
