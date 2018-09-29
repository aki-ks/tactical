package me.aki.tactical.ref.expr;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;

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

    public Cell<Expression> getValue1Cell() {
        return Cell.of(this::getValue1, this::setValue1);
    }

    public Expression getValue2() {
        return value2;
    }

    public void setValue2(Expression value2) {
        this.value2 = value2;
    }

    public Cell<Expression> getValue2Cell() {
        return Cell.of(this::getValue2, this::setValue2);
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getValue1Cell(), getValue2Cell());
    }
}
