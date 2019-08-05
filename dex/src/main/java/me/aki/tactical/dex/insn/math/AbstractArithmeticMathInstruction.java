package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.*;
import me.aki.tactical.dex.Register;

public class AbstractArithmeticMathInstruction extends AbstractBinaryMathInstruction {
    /**
     * Type that this instruction operates on.
     *
     * This may either be an {@link IntType}, {@link LongType}, {@link FloatType} or {@link DoubleType}.
     */
    private PrimitiveType type;

    public AbstractArithmeticMathInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(op1, op2, result);
        setType(type);
    }

    public PrimitiveType getType() {
        return type;
    }

    public void setType(PrimitiveType type) {
        this.type = type;
    }
}
