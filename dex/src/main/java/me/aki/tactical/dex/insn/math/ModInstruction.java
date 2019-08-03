package me.aki.tactical.dex.insn.math;

import me.aki.tactical.dex.Register;

/**
 * Calculate the remainder of the value of one register divided by the value of another one.
 */
public class ModInstruction extends AbstractBinaryMathInstruction {
    public ModInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
