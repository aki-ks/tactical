package me.aki.tactical.ref.condition;

import me.aki.tactical.ref.Expression;

/**
 * Check whether one number is greater than another.
 */
public class GreaterThan extends Condition {
    public GreaterThan(Expression value1, Expression value2) {
        super(value1, value2);
    }

    @Override
    public LessEqual negate() {
        return new LessEqual(getValue1(), getValue2());
    }
}
