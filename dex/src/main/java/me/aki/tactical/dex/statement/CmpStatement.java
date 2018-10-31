package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.Register;

/**
 * Compare two values of <tt>long</tt> type.
 */
public class CmpStatement extends AbstractCompareStatement {
    public CmpStatement(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
