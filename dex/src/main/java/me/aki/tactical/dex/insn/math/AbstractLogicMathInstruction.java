package me.aki.tactical.dex.insn.math;

import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

public class AbstractLogicMathInstruction extends AbstractBinaryMathInstruction {
    /**
     * Type that this instruction operates on.
     *
     * This may either be an {@link IntType} or {@link LongType}.
     */
    private PrimitiveType type;

    public AbstractLogicMathInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(op1, op2, result);
        setType(type);
    }

    public PrimitiveType getType() {
        return type;
    }

    public void setType(PrimitiveType type) {
        if (type instanceof IntLikeType || type instanceof LongType) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Expected an int or long type");
        }
    }
}
