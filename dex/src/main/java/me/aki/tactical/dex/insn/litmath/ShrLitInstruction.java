package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Shift the value of a {@link Register} by an integer constant to the right and store the result
 * in a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 >> op2;</code>
 *
 * @see UShrLitInstruction for a bitwise shift that does not preserve the sign.
 */
public class ShrLitInstruction extends AbstractBinaryLitMathInstruction {
    public ShrLitInstruction(Register op1, int op2, Register result) {
        super(op1, op2, result);
    }
}
