package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;

import java.util.Objects;
import java.util.Set;

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

    public RWCell<Expression> getValueCell() {
        return RWCell.of(this::getValue, this::setValue, Expression.class);
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public Set<RCell<Expression>> getReadValueCells() {
        return Set.of(getValueCell());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NegExpr negExpr = (NegExpr) o;
        return Objects.equals(value, negExpr.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
