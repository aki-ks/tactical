package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.Type;

/**
 * Pop two float/double values, compare them and push a result.
 *
 * If both numbers are equal push 0.
 * Otherwise either 1 or -1 is pushed,
 * depending on which number is greater.
 *
 * @see CmplInsn has similar functionality but handles NaN different
 */
public class CmpgInsn extends AbstractBinaryMathInsn {
    public CmpgInsn(Type type) {
        super(type);
    }

    @Override
    protected boolean isTypeSupported(Type type) {
        return type instanceof FloatType || type instanceof DoubleType;
    }
}
