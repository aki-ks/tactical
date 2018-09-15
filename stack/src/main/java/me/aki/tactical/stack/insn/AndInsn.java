package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;

/**
 * Pop two int/long values, calculate the logical and ('&' operator) and push the result.
 */
public class AndInsn extends AbstractBinaryMathInsn {
    public AndInsn(Type type) {
        super(type);
    }

    @Override
    protected boolean isTypeSupported(Type type) {
        return type instanceof IntType || type instanceof LongType;
    }
}
