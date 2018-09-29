package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.Expression;

/**
 * Bitwise shift a int or long to the left.
 */
public class ShlExpr extends AbstractBinaryExpr {
    /**
     * Create a new ShlExpr.
     *
     * @param value1 value to be shifted
     * @param value2 amount of bits to shift
     */
    public ShlExpr(Expression value1, Expression value2) {
        super(value1, value2);
    }

    @Override
    public Type getType() {
        return getValue1().getType();
    }
}
