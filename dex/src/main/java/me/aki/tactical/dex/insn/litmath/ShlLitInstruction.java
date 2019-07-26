package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Shift the value of a {@link Register} by an int constant to the left and store the result in
 * a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 << op2;</code>
 */
public class ShlLitInstruction extends AbstractBinaryLitMathInstruction {
    public ShlLitInstruction(Register op1, short op2, Register result) {
        super(op1, op2, result);
    }
}
