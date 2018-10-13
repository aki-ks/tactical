package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

public interface Statement {
    /**
     * Get all registers that this statements reads from.
     */
    List<Register> getReadRegisters();

    /**
     * Get the register that this statement writes to.
     */
    Optional<Register> getWrittenRegister();
}
