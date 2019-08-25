package me.aki.tactical.ref.expr;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;

import java.util.Objects;
import java.util.Set;

/**
 * Reusable code for expressions that operate on two values.
 */
public abstract class AbstractBinaryExpr implements Expression {
    private Expression value1;
    private Expression value2;

    public AbstractBinaryExpr(Expression value1, Expression value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public Expression getValue1() {
        return value1;
    }

    public void setValue1(Expression value1) {
        this.value1 = value1;
    }

    public RWCell<Expression> getValue1Cell() {
        return RWCell.of(this::getValue1, this::setValue1, Expression.class);
    }

    public Expression getValue2() {
        return value2;
    }

    public void setValue2(Expression value2) {
        this.value2 = value2;
    }

    public RWCell<Expression> getValue2Cell() {
        return RWCell.of(this::getValue2, this::setValue2, Expression.class);
    }

    @Override
    public Set<RCell<Expression>> getReadValueCells() {
        return Set.of(getValue1Cell(), getValue2Cell());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBinaryExpr expr = (AbstractBinaryExpr) o;
        return Objects.equals(value1, expr.value1) &&
                Objects.equals(value2, expr.value2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value1, value2);
    }

    @Override
    public String toString() {
        return AbstractBinaryExpr.class.getSimpleName() + '{' +
                "value1=" + value1 +
                ", value2=" + value2 +
                '}';
    }
}
