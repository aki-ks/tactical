package me.aki.tactical.stack.insn;

/**
 * Duplicate the upper two stack values and insert them one element deeper.
 *
 * Example:
 * - before: value1, value2, someOtherValue, ...
 * - after: value1, value2, someOtherValue, value1, value2, ...
 */
public class Dup2X1Insn extends AbstractInstruction {
    @Override
    public int getPushCount() {
        return 5;
    }

    @Override
    public int getPopCount() {
        return 3;
    }
}
