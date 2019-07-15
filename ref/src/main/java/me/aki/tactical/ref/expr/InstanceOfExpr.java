package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.Objects;

/**
 * Check whether a value is of a certain type.
 * It will either result in the int value "one" (true) or "zero" (false).
 */
public class InstanceOfExpr implements Expression {
    /**
     * Check whether the value is an instance of this type.
     */
    private RefType checkType;

    /**
     * Value whose type is checked.
     */
    private Expression value;

    public InstanceOfExpr(RefType checkType, Expression value) {
        this.checkType = checkType;
        this.value = value;
    }

    public RefType getCheckType() {
        return checkType;
    }

    public void setCheckType(RefType checkType) {
        this.checkType = checkType;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public RWCell<Expression> getValueCell() {
        return RWCell.of(this::getValue, this::setValue, Expression.class);
    }

    @Override
    public Type getType() {
        return IntType.getInstance();
    }

    @Override
    public List<RCell<Expression>> getReferencedValueCells() {
        return List.of(getValueCell());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceOfExpr expr = (InstanceOfExpr) o;
        return Objects.equals(checkType, expr.checkType) &&
                Objects.equals(value, expr.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkType, value);
    }
}
