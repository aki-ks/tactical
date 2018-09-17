package me.aki.tactical.stack.insn;

/**
 * Pop an instance of {@link Throwable} from the stack and throw that exception.
 */
public class ThrowInsn extends AbstractInstruction {
    @Override
    public int getPushCount() {
        return 0;
    }

    @Override
    public int getPopCount() {
        return 1;
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }
}
