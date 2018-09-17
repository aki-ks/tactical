package me.aki.tactical.stack.insn;

/**
 * Pop the two upper values on the stack and push them in the opposite order.
 *
 * This instruction cannot swap long or double values.
 *
 * example:
 * - before: value1, value2, ...
 * - after:  value2, value1, ...
 */
public class SwapInsn extends AbstractInstruction {
    @Override
    public int getPushCount() {
        return 2;
    }

    @Override
    public int getPopCount() {
        return 2;
    }
}
