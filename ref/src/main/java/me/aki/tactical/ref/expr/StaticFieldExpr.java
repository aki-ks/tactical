package me.aki.tactical.ref.expr;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.ref.Expression;

import java.util.Set;

/**
 * Reference the value of a static field.
 */
public class StaticFieldExpr extends AbstractFieldExpr {
    public StaticFieldExpr(FieldRef field) {
        super(field);
    }

    @Override
    public Set<RCell<Expression>> getReadValueCells() {
        return Set.of();
    }
}
