package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Variable;

import java.util.List;

/**
 * Reference a value contained in an array.
 */
public class ArrayBoxExpr implements Variable {
    /**
     * Array containing the referenced element.
     */
    private Expression array;

    /**
     * Index of the referenced element within the array.
     */
    private Expression index;

    public ArrayBoxExpr(Expression array, Expression index) {
        this.array = array;
        this.index = index;
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

    public Expression getIndex() {
        return index;
    }

    public void setIndex(Expression index) {
        this.index = index;
    }

    public Cell<Expression> getIndexCell() {
        return Cell.of(this::getIndex, this::setIndex);
    }

    @Override
    public Type getType() {
        return ((ArrayType) array.getType()).getLowerType();
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getArrayCell(), getIndexCell());
    }
}
