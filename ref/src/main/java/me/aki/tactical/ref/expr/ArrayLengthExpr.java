package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;

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

    public Cell<Expression> getArrayCell() {
        return Cell.of(this::getArray, this::setArray);
    }

    @Override
    public Type getType() {
        return IntType.getInstance();
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getArrayCell());
    }
}
