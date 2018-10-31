package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.Objects;

/**
 * Expression that casts another value to a certain type.
 */
public class CastExpr implements Expression {
    /**
     * Type that the value be casted to
     */
    private Type type;

    /**
     * Value that will be casted
     */
    private Expression value;

    public CastExpr(Type type, Expression value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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
    public List<Cell<Expression>> getReferencedValueCells() {
        return List.of(getValueCell());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CastExpr expr = (CastExpr) o;
        return Objects.equals(type, expr.type) &&
                Objects.equals(value, expr.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
