package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;

/**
 * Pop two numbers from the stack, subtract them and push the result onto the stack.
 */
public class SubInsn extends AbstractBinaryMathInsn {
    public SubInsn(Type type) {
        super(type);
    }

    @Override
    protected boolean isTypeSupported(Type type) {
        return type instanceof IntType || type instanceof LongType ||
                type instanceof FloatType || type instanceof DoubleType;
    }
}
