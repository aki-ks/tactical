package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Calculate the bitwise <tt>or</tt> of two values of two registers storing the result in of another register.
 */
public class OrInstruction extends AbstractLogicMathInstruction {
    public OrInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(type, op1, op2,result);
    }
}
