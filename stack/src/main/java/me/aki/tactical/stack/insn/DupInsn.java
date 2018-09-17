package me.aki.tactical.stack.insn;

/**
 * Pop a value from the stack and push it two times.
 *
 * example:
 * - before: value, ...
 * - after: value, value, ...
 */
public class DupInsn extends AbstractInstruction {
    @Override
    public int getPushCount() {
        return 2;
    }

    @Override
    public int getPopCount() {
        return 1;
    }
}
