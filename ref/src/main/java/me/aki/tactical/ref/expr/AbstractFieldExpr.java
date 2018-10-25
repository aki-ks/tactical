package me.aki.tactical.ref.expr;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.Variable;

public abstract class AbstractFieldExpr implements Variable {
    /**
     * The referenced field.
     */
    private FieldRef field;

    public AbstractFieldExpr(FieldRef field) {
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
}
