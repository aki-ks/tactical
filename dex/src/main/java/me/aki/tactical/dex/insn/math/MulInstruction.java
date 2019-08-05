package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Multiply the value of two registers storing them in another register.
 */
public class MulInstruction extends AbstractArithmeticMathInstruction {
    public MulInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(type, op1, op2, result);
    }
}
