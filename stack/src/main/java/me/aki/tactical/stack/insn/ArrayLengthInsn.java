package me.aki.tactical.stack.insn;

/**
 * Pop an array from the stack and push its length.
 */
public class ArrayLengthInsn implements Instruction {
    @Override
    public int getPushCount() {
        return 1;
    }

    @Override
    public int getPopCount() {
        return 1;
    }
}
