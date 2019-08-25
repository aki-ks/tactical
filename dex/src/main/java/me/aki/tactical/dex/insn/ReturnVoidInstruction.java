package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.Optional;
import java.util.Set;

/**
 * Return from a 'void' method.
 *
 * @see ReturnInstruction to return from non-void methods.
 */
public class ReturnVoidInstruction implements Instruction {
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
