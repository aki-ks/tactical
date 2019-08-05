package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Shift the value of a by the value of another one to the left.
 */
public class ShlInstruction extends AbstractLogicMathInstruction {
    public ShlInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(type, op1, op2, result);
    }
}
