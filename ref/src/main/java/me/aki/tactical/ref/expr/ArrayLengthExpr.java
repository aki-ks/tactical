package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.Objects;

/**
 * Get the length of an array.
 */
public class ArrayLengthExpr implements Expression {
    /**
     * Array whose length is requested.
     */
    private Expression array;

    public ArrayLengthExpr(Expression array) {
        this.array = array;
    }

    public Expression getArray() {
        return array;
    }

    public void setArray(Expression array) {
        this.array = array;
    }

    public RWCell<Expression> getArrayCell() {
        return RWCell.of(this::getArray, this::setArray, Expression.class);
    }

    @Override
    public Type getType() {
        return IntType.getInstance();
    }

    @Override
    public List<RCell<Expression>> getReferencedValueCells() {
        return List.of(getArrayCell());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayLengthExpr expr = (ArrayLengthExpr) o;
        return Objects.equals(array, expr.array);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array);
    }
}
