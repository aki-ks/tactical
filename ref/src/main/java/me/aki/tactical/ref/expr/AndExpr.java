package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.Expression;

/**
 * Instructions that calculates the 'logical and' of two numeric values.
 */
public class AndExpr extends AbstractBinaryExpr {
    public AndExpr(Expression value1, Expression value2) {
        super(value1, value2);
    }

    @Override
    public Type getType() {
        Type type1 = getValue1().getType();
        Type type2 = getValue2().getType();
        if (type1.equals(type2)) {
            return type1;
        } else {
            throw new IllegalStateException("Cannot and values of types " + type1 + " and " + type2);
        }
    }
}
