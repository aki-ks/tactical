package me.aki.tactical.ref.condition;

import me.aki.tactical.ref.Expression;

/**
 * Check whether one number is greater or equal than another.
 */
public class GreaterEqual extends Condition {
    public GreaterEqual(Expression value1, Expression value2) {
        super(value1, value2);
    }

    @Override
    public LessThan negate() {
        return new LessThan(getValue1(), getValue2());
    }
}
