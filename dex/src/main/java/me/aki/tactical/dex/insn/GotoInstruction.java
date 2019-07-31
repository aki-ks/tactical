package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
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
    private Instruction target;

    public GotoInstruction(Instruction target) {
        this.target = target;
    }

    public Instruction getTarget() {
        return target;
    }

    public void setTarget(Instruction target) {
        this.target = target;
    }

    public RWCell<Instruction> getTargetCell() {
        return RWCell.of(this::getTarget, this::setTarget, Instruction.class);
    }

    @Override
    public List<Instruction> getBranchTargets() {
        return List.of(target);
    }

    @Override
    public List<RWCell<Instruction>> getBranchTargetCells() {
        return List.of(getTargetCell());
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }
}
