package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Multiply the content of one {@link Register} by an integer literal and store the result
 * in a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 * op2;</code>
 */
public class MulLitInstruction extends AbstractBinaryLitMathInstruction {
    public MulLitInstruction(Register op1, short op2, Register result) {
        super(op1, op2, result);
    }
}
