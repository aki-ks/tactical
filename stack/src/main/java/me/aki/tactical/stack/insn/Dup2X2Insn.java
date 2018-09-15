package me.aki.tactical.stack.insn;

/**
 * Duplicate the upper two stack values and insert them two elements deeper.
 *
 * Example:
 * - before: value1, value2, otherValue1, otherValue2, ...
 * - after: value1, value2, otherValue1, otherValue2, value1, value2, ...
 */
public class Dup2X2Insn implements Instruction {
    @Override
    public int getPushCount() {
        return 6;
    }

    @Override
    public int getPopCount() {
        return 4;
    }
}
