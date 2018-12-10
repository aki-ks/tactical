package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Calculate the bitwise <tt>or</tt> of two values of two registers storing the result in of another register.
 */
public class OrInstruction extends AbstractBinaryMathInstruction {
    public OrInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(type, op1, op2,result);
    }

    @Override
    protected boolean isTypeSupported(PrimitiveType type) {
        return type instanceof IntLikeType || type instanceof LongType;
    }
}
