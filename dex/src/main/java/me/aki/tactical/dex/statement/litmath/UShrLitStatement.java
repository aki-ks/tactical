package me.aki.tactical.dex.statement.litmath;

import me.aki.tactical.dex.Register;

/**
 * Shift the value of a {@link Register} by an int constant to the right and store the result in
 * a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 >>> op2;</code>
 *
 * @see ShrLitStatement for an arithmetic shift that preserves the sign
 */
public class UShrLitStatement extends AbstractBinaryLitMathStatement {
    public UShrLitStatement(Register op1, int op2, Register result) {
        super(op1, op2, result);
    }
}
