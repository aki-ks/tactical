package me.aki.tactical.stack.insn;

/**
 * Pop two values of type 'long' from the stack,
 * compare them and push a result of int type.
 *
 * If both numbers are equal push 0.
 * Otherwise either 1 or -1 is pushed,
 * depending on which number is greater.
 */
public class CmpInsn implements Instruction {
    @Override
    public int getPushCount() {
        return 1;
    }

    @Override
    public int getPopCount() {
        return 2;
    }
}
