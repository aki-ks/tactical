package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;

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
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getValueCell());
    }
}
