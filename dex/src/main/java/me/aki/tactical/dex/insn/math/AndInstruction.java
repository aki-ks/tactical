package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Calculate the bitwise <tt>and</tt> of one {@link Register} and the value of another one.
 *
 * Can be expressed as:
 * <code>op1 = op1 & op2;</code>
 */
public class AndInstruction extends AbstractBinaryMathInstruction {
    public AndInstruction(PrimitiveType type, Register op1, Register op2) {
        super(type, op1, op2);
    }

    @Override
    protected boolean isTypeSupported(PrimitiveType type) {
        return type instanceof IntLikeType || type instanceof LongType ||
                type instanceof FloatType || type instanceof DoubleType;
    }
}
