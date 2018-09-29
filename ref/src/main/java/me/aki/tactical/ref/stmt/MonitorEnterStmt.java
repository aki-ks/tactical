package me.aki.tactical.ref.stmt;

import me.aki.tactical.ref.Expression;

/**
 * Acquire a lock on an object.
 *
 * @see MonitorExitStmt to release a lock
 */
public class MonitorEnterStmt extends AbstractUnaryStmt {
    public MonitorEnterStmt(Expression value) {
        super(value);
    }
}
