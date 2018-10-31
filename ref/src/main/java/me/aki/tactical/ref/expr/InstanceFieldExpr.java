package me.aki.tactical.ref.expr;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.Objects;

/**
 * Reference the value of a non-static field.
 */
public class InstanceFieldExpr extends AbstractFieldExpr {
    /**
     * Instance of the class containing the field.
     */
    private Expression instance;

    public InstanceFieldExpr(FieldRef field, Expression instance) {
        super(field);
        this.instance = instance;
    }

    public Expression getInstance() {
        return instance;
    }

    public void setInstance(Expression instance) {
        this.instance = instance;
    }

    public Cell<Expression> getInstanceCell() {
        return Cell.of(this::getInstance, this::setInstance, Expression.class);
    }

    @Override
    public List<Cell<Expression>> getReferencedValueCells() {
        return List.of(getInstanceCell());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InstanceFieldExpr fieldExpr = (InstanceFieldExpr) o;
        return Objects.equals(instance, fieldExpr.instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instance);
    }
}
