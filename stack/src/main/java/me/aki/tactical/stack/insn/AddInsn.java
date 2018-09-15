package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;

/**
 * Pop two numbers of from the stack, add them and push the result.
 */
public class AddInsn extends AbstractBinaryMathInsn {
    public AddInsn(Type type) {
        super(type);
        if (!(type instanceof IntType) && !(type instanceof LongType) &&
                !(type instanceof FloatType) && !(type instanceof DoubleType)) {
            throw new IllegalArgumentException(type + " cannot be computed by add instruction");
        }
    }
}
