package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Compare two <tt>float</tt> or <tt>double</tt> values.
 *
 * @see CmplInstruction handles <tt>NaN</tt> values different
 */
public class CmpgInstruction extends AbstractCompareInstruction {
    /**
     * Type that this instruction operates on.
     *
     * This should either be {@link LongType#getInstance()} or {@link DoubleType#getInstance()}.
     */
    private PrimitiveType type;

    public CmpgInstruction(PrimitiveType type, Register op1, Register op2, Register result) {
        super(op1, op2, result);
        setType(type);
    }

    public PrimitiveType getType() {
        return type;
    }

    public void setType(PrimitiveType type) {
        if (type instanceof LongType || type instanceof DoubleType) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Expected long or double type");
        }
    }
}
