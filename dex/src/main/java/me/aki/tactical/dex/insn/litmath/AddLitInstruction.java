package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Add the value of a {@link Register} and an integer literal and store the result in a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 + op2;</code>
 */
public class AddLitInstruction extends AbstractBinaryLitMathInstruction {
    public AddLitInstruction(Register op1, short op2, Register result) {
        super(op1, op2, result);
    }
}
