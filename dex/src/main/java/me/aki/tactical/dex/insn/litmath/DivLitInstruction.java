package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Divide the value of a {@link Register} by an integer literal and store the result
 * in a {@link Register}.
 *
 * Can be expressed as:
 * <code>result = op1 / op2;</code>
 */
public class DivLitInstruction extends AbstractBinaryLitMathInstruction {
    public DivLitInstruction(Register op1, int op2, Register result) {
        super(op1, op2, result);
    }
}
