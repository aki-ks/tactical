package me.aki.tactical.dex.insn.math;

import me.aki.tactical.dex.Register;

/**
 * Add the value of two registers and store it in another one.
 */
public class AddInstruction extends AbstractBinaryMathInstruction {
    public AddInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
