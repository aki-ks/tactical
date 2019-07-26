package me.aki.tactical.dex.insn;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

public interface Instruction {
    /**
     * Get all registers that this instruction reads from.
     */
    List<Register> getReadRegisters();

    /**
     * Get the register that this instruction writes to.
     */
    Optional<Register> getWrittenRegister();

    /**
     * Is the succeeding instruction reachable from this insn.
     */
    default boolean continuesExecution() {
        return true;
    }
}
