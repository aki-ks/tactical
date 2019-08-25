package me.aki.tactical.ref.condition;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Referencing;

import java.util.Objects;
import java.util.Set;

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
