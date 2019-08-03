package me.aki.tactical.dex.insn.math;

import me.aki.tactical.dex.Register;

/**
 * Shift the value of a by the value of another one to the left.
 */
public class ShlInstruction extends AbstractBinaryMathInstruction {
    public ShlInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
