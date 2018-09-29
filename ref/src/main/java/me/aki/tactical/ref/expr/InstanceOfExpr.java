package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;

/**
 * Check whether a value is of a certain type.
 * It will either result in the int value "one" (true) or "zero" (false).
 */
public class InstanceOfExpr implements Expression {
    /**
     * Check whether the value is an instance of this type
     */
    private Type checkType;

    /**
     * Value whose type is checked
     */
    private Expression value;

    public InstanceOfExpr(Type checkType, Expression value) {
        this.checkType = checkType;
        this.value = value;
    }

    public Type getCheckType() {
        return checkType;
    }

    public void setCheckType(Type checkType) {
        this.checkType = checkType;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public Cell<Expression> getValueCell() {
        return Cell.of(this::getValue, this::setValue);
    }

    @Override
    public Type getType() {
        return IntType.getInstance();
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getValueCell());
    }
}
