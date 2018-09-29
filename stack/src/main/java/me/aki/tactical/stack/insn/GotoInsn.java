package me.aki.tactical.stack.insn;

import me.aki.tactical.core.util.Cell;

import java.util.List;

/**
 * Unconditionally jump to another instruction.
 */
public class GotoInsn extends AbstractInstruction implements BranchInsn {
    /**
     * Instruction that will be executed after this one.
     */
    private Instruction target;

    public GotoInsn(Instruction target) {
        this.target = target;
    }

    public Instruction getTarget() {
        return target;
    }

    public void setTarget(Instruction target) {
        this.target = target;
    }

    public Cell<Instruction> getTargetCell() {
        return Cell.of(this::getTarget, this::setTarget, Instruction.class);
    }

    @Override
    public int getPushCount() {
        return 0;
    }

    @Override
    public int getPopCount() {
        return 0;
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }

    @Override
    public List<Instruction> getBranchTargets() {
        return List.of(getTarget());
    }

    @Override
    public List<Cell<Instruction>> getBranchTargetCells() {
        return List.of(getTargetCell());
    }
}
