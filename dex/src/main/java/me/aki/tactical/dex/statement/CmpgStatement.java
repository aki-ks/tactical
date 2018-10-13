package me.aki.tactical.dex.statement;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.dex.Register;

/**
 * Compare two <tt>float</tt> or <tt>double</tt> values.
 *
 * @see CmplStatement handles <tt>NaN</tt> values different
 */
public class CmpgStatement extends AbstractCompareStatement {
    /**
     * Type of the values to compare.
     */
    private PrimitiveType type;

    public CmpgStatement(PrimitiveType type, Register op1, Register op2, Register result) {
        super(op1, op2, result);
        setType(type);
    }

    public PrimitiveType getType() {
        return type;
    }

    public void setType(PrimitiveType type) {
        if (!(type instanceof DoubleType) && !(type instanceof FloatType)) {
            throw new IllegalArgumentException("Can only cmpg float or double values");
        }
        this.type = type;
    }
}
