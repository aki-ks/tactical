package me.aki.tactical.dex.statement.litmath;

import me.aki.tactical.dex.Register;

/**
 * Shift the value of a {@link Register} by an integer constant to the right and store the result
 * in a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 >> op2;</code>
 *
 * @see UShrLitStatement for a bitwise shift that does not preserve the sign.
 */
public class ShrLitStatement extends AbstractBinaryLitMathStatement {
    public ShrLitStatement(Register op1, int op2, Register result) {
        super(op1, op2, result);
    }
}
