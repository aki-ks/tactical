package me.aki.tactical.ref.expr;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;

import java.util.Objects;
import java.util.Set;

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

    public RWCell<Expression> getInstanceCell() {
        return RWCell.of(this::getInstance, this::setInstance, Expression.class);
    }

    @Override
    public Set<RCell<Expression>> getReadValueCells() {
        return Set.of(getInstanceCell());
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
