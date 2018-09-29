package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.Expression;

/**
 * Compare two long values.
 *
 * If both numbers are equal it evaluates to 0.
 * Otherwise it evaluates to either 1 or -1
 * depending on which number is greater.
 */
public class CmpExpr extends AbstractBinaryExpr {
    public CmpExpr(Expression value1, Expression value2) {
        super(value1, value2);
    }

    @Override
    public Type getType() {
        return IntType.getInstance();
    }
}
