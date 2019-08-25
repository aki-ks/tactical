package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;

import java.util.Set;

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

    public RWCell<Statement> getTargetCell() {
        return RWCell.of(this::getTarget, this::setTarget, Statement.class);
    }

    @Override
    public Set<RCell<Expression>> getReadValueCells() {
        return Set.of();
    }

    @Override
    public Set<Statement> getBranchTargets() {
        return Set.of(getTarget());
    }

    @Override
    public Set<RWCell<Statement>> getBranchTargetsCells() {
        return Set.of(getTargetCell());
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }
}
