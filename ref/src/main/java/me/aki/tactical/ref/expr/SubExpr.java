package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.Expression;

/**
 * Instructions that subtracts two numeric values.
 */
public class SubExpr extends AbstractBinaryExpr {
    public SubExpr(Expression value1, Expression value2) {
        super(value1, value2);
    }

    @Override
    public Type getType() {
        // both values should have the same type
        return getValue1().getType();
    }
}
