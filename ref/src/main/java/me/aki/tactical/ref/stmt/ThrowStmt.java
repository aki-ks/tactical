package me.aki.tactical.ref.stmt;

import me.aki.tactical.ref.Expression;

/**
 * Throw an exception
 */
public class ThrowStmt extends AbstractUnaryStmt {
    public ThrowStmt(Expression value) {
        super(value);
    }
}
