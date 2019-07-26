package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Shift the value of a {@link Register} by an int constant to the right and store the result in
 * a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 >>> op2;</code>
 *
 * @see ShrLitInstruction for an arithmetic shift that preserves the sign
 */
public class UShrLitInstruction extends AbstractBinaryLitMathInstruction {
    public UShrLitInstruction(Register op1, short op2, Register result) {
        super(op1, op2, result);
    }
}
