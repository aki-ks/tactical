package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;

import java.util.List;

/**
 * Unconditionally branch to another statement.
 */
public class GotoStmt implements BranchStmt {
    /**
     * Branch to this statement.
     */
    private Statement target;

    public GotoStmt(Statement target) {
        this.target = target;
    }

    public Statement getTarget() {
        return target;
    }

    public void setTarget(Statement target) {
        this.target = target;
    }

    public Cell<Statement> getTargetCell() {
        return Cell.of(this::getTarget, this::setTarget, Statement.class);
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of();
    }

    @Override
    public List<Cell<Statement>> getBranchTargets() {
        return List.of(getTargetCell());
    }
}
