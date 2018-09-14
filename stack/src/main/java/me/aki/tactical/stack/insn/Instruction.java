package me.aki.tactical.stack.insn;

public interface Instruction {
    /**
     * Get the amount of values that this instructions pushes onto the stack.
     *
     * @return amount of pushed values
     */
    int getPushCount();

    /**
     * Get the amount of values that this instructions pops from the stack.
     *
     * @return amount of popped values
     */
    int getPopCount();

    /**
     * How will the size of the stack change after this instruction was executed.
     * Negative values mean that more values are popped than pushed.
     *
     * @return change of stack size
     */
    default int stackSizeDelta() {
        return getPushCount() - getPopCount();
    }
}
