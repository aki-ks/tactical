package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Calculate the bitwise <tt>xor</tt> of one {@link Register} and an int constant and store the
 * result in a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 ^ op2;</code>
 */
public class XorLitInstruction extends AbstractBinaryLitMathInstruction {
    public XorLitInstruction(Register op1, int op2, Register result) {
        super(op1, op2, result);
    }
}
