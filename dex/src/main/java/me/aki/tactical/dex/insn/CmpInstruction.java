package me.aki.tactical.dex.insn;

import me.aki.tactical.dex.Register;

/**
 * Compare two values of <tt>long</tt> type.
 */
public class CmpInstruction extends AbstractCompareInstruction {
    public CmpInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
