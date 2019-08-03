package me.aki.tactical.dex.insn.math;

import me.aki.tactical.dex.Register;

/**
 * Divide the content of one register by the value of another one.
 */
public class DivInstruction extends AbstractBinaryMathInstruction {
    public DivInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
