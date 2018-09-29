package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;

/**
 * Negate the value of another (numeric) expression.
 */
public class NegExpr implements Expression {
    /**
     * Number to be negated
     */
    private Expression value;

    public NegExpr(Expression value) {
        this.value = value;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public Cell<Expression> getValueCell() {
        return Cell.of(this::getValue, this::setValue, Expression.class);
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getValueCell());
    }
}
