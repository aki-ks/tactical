package me.aki.tactical.dex.insn;

import me.aki.tactical.dex.Register;

/**
 * Compare two <tt>float</tt> or <tt>double</tt> values.
 *
 * @see CmpgInstruction handles <tt>NaN</tt> values different
 */
public class CmplInstruction extends AbstractCompareInstruction {
    public CmplInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
