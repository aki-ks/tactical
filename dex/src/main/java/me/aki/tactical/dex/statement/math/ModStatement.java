package me.aki.tactical.dex.statement.math;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Replace a value within a {@link Register} against the remainder of the current value divided by
 * the value of another {@link Register}.
 *
 * Can be expressed as:
 * <code>op1 = op1 % op2;</code>
 */
public class ModStatement extends AbstractBinaryMathStatement {
    public ModStatement(PrimitiveType type, Register op1, Register op2) {
        super(type, op1, op2);
    }

    @Override
    protected boolean isTypeSupported(PrimitiveType type) {
        return type instanceof IntLikeType || type instanceof LongType ||
                type instanceof FloatType || type instanceof DoubleType;
    }
}
