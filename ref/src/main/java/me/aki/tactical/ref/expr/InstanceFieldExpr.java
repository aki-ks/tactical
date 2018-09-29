package me.aki.tactical.ref.expr;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Variable;

import java.util.List;

/**
 * Reference the value of a non-static field.
 */
public class InstanceFieldExpr implements Variable {
    /**
     * The referenced field.
     */
    private FieldRef field;

    /**
     * Instance of the class containing the field.
     */
    private Expression instance;

    public InstanceFieldExpr(FieldRef field, Expression instance) {
        this.field = field;
        this.instance = instance;
    }

    public FieldRef getField() {
        return field;
    }

    public void setField(FieldRef field) {
        this.field = field;
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
    public Type getType() {
        return field.getType();
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getInstanceCell());
    }
}
