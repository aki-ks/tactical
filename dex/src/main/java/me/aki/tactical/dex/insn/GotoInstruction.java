package me.aki.tactical.dex.insn;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Unconditional branch to another instruction.
 */
public class GotoInstruction implements BranchInstruction {
    /**
     * Branch to this instruction.
     */
    private Instruction location;

    public GotoInstruction(Instruction location) {
        this.location = location;
    }

    @Override
    public List<Instruction> getBranchTargets() {
        return List.of(location);
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }
}
