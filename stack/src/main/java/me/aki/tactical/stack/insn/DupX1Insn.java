package me.aki.tactical.stack.insn;

/**
 * Duplicate the upper value, but insert it one elements deeper.
 *
 * Example:
 * - before: value, someOtherValue, ...
 * - after: value, someOtherValue, value, ...
 */
public class DupX1Insn implements Instruction {
    @Override
    public int getPushCount() {
        return 3;
    }

    @Override
    public int getPopCount() {
        return 2;
    }
}
