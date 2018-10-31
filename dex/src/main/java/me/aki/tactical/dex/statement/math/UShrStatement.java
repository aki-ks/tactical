package me.aki.tactical.dex.statement.math;

import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Shift the value within a {@link Register} by the value of another one to the right.
 *
 * Can be expressed as:
 * <code>op1 = op1 >>> op2;</code>
 *
 * @see ShrStatement for an arithmetic shift that preserves the sign
 */
public class UShrStatement extends AbstractBinaryMathStatement {
    public UShrStatement(PrimitiveType type, Register op1, Register op2) {
        super(type, op1, op2);
    }

    @Override
    protected boolean isTypeSupported(PrimitiveType type) {
        return type instanceof IntLikeType || type instanceof LongType;
    }
}