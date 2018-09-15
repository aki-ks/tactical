package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.Type;

/**
 * An instruction that takes two values from the stack,
 * does a computation and pushes the result.
 */
public abstract class AbstractBinaryMathInsn implements Instruction {
    /**
     * Types that this instruction may operate on.
     */
    private Type type;

    public AbstractBinaryMathInsn(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public int getPushCount() {
        return 1;
    }

    @Override
    public int getPopCount() {
        return 2;
    }
}
