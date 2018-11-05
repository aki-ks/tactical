package me.aki.tactical.stack.insn;

import me.aki.tactical.stack.StackLocal;

import java.util.Objects;

/**
 * Increment a local containing an int by a statically known value.
 */
public class IncrementInsn extends AbstractInstruction {
    /**
     * Local whose value will be incremented
     */
    private StackLocal local;

    /**
     * Value by which the local will be in-/decremented.
     */
    private int value;

    public IncrementInsn(StackLocal local, int value) {
        this.local = local;
        this.value = value;
    }

    public StackLocal getLocal() {
        return local;
    }

    public void setLocal(StackLocal local) {
        this.local = local;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public int getPushCount() {
        return 0;
    }

    @Override
    public int getPopCount() {
        return 0;
    }
}
