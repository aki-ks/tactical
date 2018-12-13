package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.Cell;
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

    public Instruction getLocation() {
        return location;
    }

    public void setLocation(Instruction location) {
        this.location = location;
    }

    public Cell<Instruction> getLocationCell() {
        return Cell.of(this::getLocation, this::setLocation, Instruction.class);
    }

    @Override
    public List<Instruction> getBranchTargets() {
        return List.of(location);
    }

    @Override
    public List<Cell<Instruction>> getBranchTargetCells() {
        return List.of(getLocationCell());
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
