package me.aki.tactical.ref.expr;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Variable;

import java.util.List;

/**
 * Reference the value of a static field.
 */
public class StaticFieldExpr implements Variable {
    /**
     * The referenced field.
     */
    private FieldRef field;

    public StaticFieldExpr(FieldRef field) {
        this.field = field;
    }

    public FieldRef getField() {
        return field;
    }

    public void setField(FieldRef field) {
        this.field = field;
    }

    @Override
    public Type getType() {
        return field.getType();
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of();
    }
}
