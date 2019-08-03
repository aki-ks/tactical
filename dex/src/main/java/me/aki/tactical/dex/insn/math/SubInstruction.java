package me.aki.tactical.dex.insn.math;

import me.aki.tactical.dex.Register;

/**
 * Calculate the difference between the values in two registers.
 */
public class SubInstruction extends AbstractBinaryMathInstruction {
    public SubInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
