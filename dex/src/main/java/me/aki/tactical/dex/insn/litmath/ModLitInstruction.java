package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Calculate the remainder of one {@link Register} divided by an integer literal and store the
 * result in another {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 % op2;</code>
 */
public class ModLitInstruction extends AbstractBinaryLitMathInstruction {
    public ModLitInstruction(Register op1, int op2, Register result) {
        super(op1, op2, result);
    }
}
