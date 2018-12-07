package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Calculate the bitwise <tt>and</tt> of one {@link Register} and an integer literal and store the
 * result in another {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 & op2;</code>
 */
public class AndLitInstruction extends AbstractBinaryLitMathInstruction {
    public AndLitInstruction(Register op1, int op2, Register result) {
        super(op1, op2, result);
    }
}
