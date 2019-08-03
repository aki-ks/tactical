package me.aki.tactical.dex.insn.math;

import me.aki.tactical.dex.Register;

/**
 * Shift the value of a register by the value of another one to the right.
 *
 * @see UShrInstruction for a bitwise shift that does not preserve the sign.
 */
public class ShrInstruction extends AbstractBinaryMathInstruction {
    public ShrInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
