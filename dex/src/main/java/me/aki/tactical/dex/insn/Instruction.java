package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

public interface Instruction {
    /**
     * Get all registers that this instruction reads from.
     */
    List<Register> getReadRegisters();

    /**
     * Get cells of all registers that this instruction reads from.
     */
    List<RWCell<Register>> getReadRegisterCells();

    /**
     * Get the register that this instruction writes to.
     */
    Optional<Register> getWrittenRegister();

    /**
     * Get a cell of the register that this instruction writes to.
     */
    Optional<RWCell<Register>> getWrittenRegisterCell();

    /**
     * Is the succeeding instruction reachable from this insn.
     */
    default boolean continuesExecution() {
        return true;
    }
}
