package me.aki.tactical.ref.condition;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Referencing;

import java.util.List;
import java.util.Objects;

/**
 * A condition that compares two values.
 */
public abstract class Condition implements Referencing {
    private Expression value1;
    private Expression value2;

    public Condition(Expression value1, Expression value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public Expression getValue1() {
        return value1;
    }

    public void setValue1(Expression value1) {
        this.value1 = value1;
    }

    public Cell<Expression> getValue1Cell() {
        return Cell.of(this::getValue1, this::setValue1, Expression.class);
    }

    public Expression getValue2() {
        return value2;
    }

    public void setValue2(Expression value2) {
        this.value2 = value2;
    }

    public Cell<Expression> getValue2Cell() {
        return Cell.of(this::getValue2, this::setValue2, Expression.class);
    }

    @Override
    public List<Cell<Expression>> getReferencedValueCells() {
        return List.of(getValue1Cell(), getValue2Cell());
    }

    public abstract Condition negate();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;
        return Objects.equals(value1, condition.value1) &&
                Objects.equals(value2, condition.value2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value1, value2);
    }
}
