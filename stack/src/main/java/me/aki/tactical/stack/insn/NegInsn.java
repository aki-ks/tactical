package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;

/**
 * Pop one number from the stack and push it with the opposite sign.
 */
public class NegInsn implements Instruction {
    /**
     * Type of number that is negated.
     */
    private Type type;

    public NegInsn(Type type) {
        this.type = type;
        if (!(type instanceof IntType) && !(type instanceof LongType) &&
                !(type instanceof FloatType) && !(type instanceof DoubleType)) {
            throw new IllegalArgumentException(type + " is not supported by the negate instruction");
        }
    }

    @Override
    public int getPushCount() {
        return 1;
    }

    @Override
    public int getPopCount() {
        return 1;
    }
}
