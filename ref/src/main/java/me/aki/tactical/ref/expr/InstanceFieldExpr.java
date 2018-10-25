package me.aki.tactical.ref.expr;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;

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
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getInstanceCell());
    }
}
