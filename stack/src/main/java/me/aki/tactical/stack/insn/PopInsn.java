package me.aki.tactical.stack.insn;

/**
 * Pop two upper value from the stack.
 */
public class PopInsn implements Instruction {
    @Override
    public int getPushCount() {
        return 0;
    }

    @Override
    public int getPopCount() {
        return 1;
    }
}
