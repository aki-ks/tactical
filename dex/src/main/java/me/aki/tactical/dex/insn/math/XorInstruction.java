package me.aki.tactical.dex.insn.math;

import me.aki.tactical.dex.Register;

/**
 * Calculate the bitwise <tt>xor</tt> of the values of two registers.
 */
public class XorInstruction extends AbstractBinaryMathInstruction {
    public XorInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
