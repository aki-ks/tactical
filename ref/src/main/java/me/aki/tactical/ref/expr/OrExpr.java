package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.Expression;

/**
 * Instructions that calculates the 'logical or' of two numeric values.
 */
public class OrExpr extends AbstractBinaryExpr {
    public OrExpr(Expression value1, Expression value2) {
        super(value1, value2);
    }

    @Override
    public Type getType() {
        Type type1 = getValue1().getType();
        Type type2 = getValue2().getType();
        if (type1.equals(type2)) {
            return type1;
        } else {
            throw new IllegalStateException("Cannot or values of types " + type1 + " and " + type2);
        }
    }
}
