package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;

/**
 * Pop a long/int and a int value from the stack.
 * Shift the bits of the long/int value to the left and push the result.
 */
public class ShlInsn extends AbstractBinaryMathInsn {
    public ShlInsn(Type type) {
        super(type);
        if (!(type instanceof IntType) && !(type instanceof LongType)) {
            throw new IllegalArgumentException(type + " cannot be computed by shl instruction");
        }
    }
}
