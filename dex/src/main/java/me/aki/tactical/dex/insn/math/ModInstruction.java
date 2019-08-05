package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Calculate the remainder of the value of one register divided by the value of another one.
 */
public class ModInstruction extends AbstractArithmeticMathInstruction {
    public ModInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(type, op1, op2, result);
    }
}
