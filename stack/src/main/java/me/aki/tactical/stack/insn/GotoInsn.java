package me.aki.tactical.stack.insn;

/**
 * Unconditionally jump to another instruction.
 */
public class GotoInsn implements BranchInsn {
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
}