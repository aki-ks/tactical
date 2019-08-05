package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Shift the value of a register by the value of another one.
 *
 * @see ShrInstruction for an arithmetic shift that preserves the sign
 */
public class UShrInstruction extends AbstractLogicMathInstruction {
    public UShrInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(type, op1, op2, result);
    }
}
