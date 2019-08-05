package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Add the value of two registers and store it in another one.
 */
public class AddInstruction extends AbstractArithmeticMathInstruction {
    public AddInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(type, op1, op2, result);
    }
}
