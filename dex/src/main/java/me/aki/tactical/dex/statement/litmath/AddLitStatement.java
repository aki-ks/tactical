package me.aki.tactical.dex.statement.litmath;

import me.aki.tactical.dex.Register;

/**
 * Add the value of a {@link Register} and an integer literal and store the result in a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 + op2;</code>
 */
public class AddLitStatement extends AbstractBinaryLitMathStatement {
    public AddLitStatement(Register op1, int op2, Register result) {
        super(op1, op2, result);
    }
}
