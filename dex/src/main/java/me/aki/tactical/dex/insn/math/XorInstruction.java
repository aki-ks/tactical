package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Calculate the bitwise <tt>xor</tt> of the values of two registers.
 */
public class XorInstruction extends AbstractLogicMathInstruction {
    public XorInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(type, op1, op2, result);
    }
}
