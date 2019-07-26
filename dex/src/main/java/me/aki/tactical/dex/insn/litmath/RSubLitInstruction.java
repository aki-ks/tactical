package me.aki.tactical.dex.insn.litmath;

import me.aki.tactical.dex.Register;

/**
 * Subtract the value of a {@link Register} from a constant and store the result in a {@link Register}.
 *
 * Note that <tt>op1</tt> is subtracted from <tt>op2</tt>, not the other way round.
 * That's why the instruction is called "reverse subtract".
 *
 * Can be expressed as:
 * <code>result = op2 - op1;</code>
 */
public class RSubLitInstruction extends AbstractBinaryLitMathInstruction {
    public RSubLitInstruction(Register op1, short op2, Register result) {
        super(op1, op2, result);
    }
}
