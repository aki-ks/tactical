package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.Optional;
import java.util.Set;

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
    public Set<Instruction> getBranchTargets() {
        return Set.of(target);
    }

    @Override
    public Set<RWCell<Instruction>> getBranchTargetCells() {
        return Set.of(getTargetCell());
    }

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
