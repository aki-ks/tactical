package me.aki.tactical.dex.insn.math;

import me.aki.tactical.dex.Register;

/**
 * Calculate the bitwise <tt>and</tt> of the value in two registers and storing the result in another register.
 */
public class AndInstruction extends AbstractBinaryMathInstruction {
    public AndInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
