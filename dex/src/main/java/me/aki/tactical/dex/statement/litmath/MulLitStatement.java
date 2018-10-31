package me.aki.tactical.dex.statement.litmath;

import me.aki.tactical.dex.Register;

/**
 * Multiply the content of one {@link Register} by an integer literal and store the result
 * in a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 * op2;</code>
 */
public class MulLitStatement extends AbstractBinaryLitMathStatement {
    public MulLitStatement(Register op1, int op2, Register result) {
        super(op1, op2, result);
    }
}
