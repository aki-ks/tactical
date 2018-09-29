package me.aki.tactical.ref.stmt;

import me.aki.tactical.ref.Expression;

/**
 * Release a lock on an object.
 *
 * @see MonitorEnterStmt to gain a lock
 */
public class MonitorExitStmt extends AbstractUnaryStmt {
    public MonitorExitStmt(Expression value) {
        super(value);
    }
}
