package me.aki.tactical.stack.insn;

/**
 * Duplicate the upper value, but insert it two elements deeper.
 *
 * Example:
 * - before: value, otherValue1, otherValue2 ...
 * - after: value, otherValue1, otherValue2, value, ...
 */
public class DupX2Insn extends AbstractInstruction {
    @Override
    public int getPushCount() {
        return 4;
    }

    @Override
    public int getPopCount() {
        return 3;
    }
}
