package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Multiply the value of two registers storing them in another register.
 */
public class MulInstruction extends AbstractBinaryMathInstruction {
    public MulInstruction(Register op1, Register op2, Register result) {
        super(op1, op2, result);
    }
}
