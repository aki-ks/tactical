package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
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

    public RWCell<Statement> getTargetCell() {
        return RWCell.of(this::getTarget, this::setTarget, Statement.class);
    }

    @Override
    public List<RCell<Expression>> getReadValueCells() {
        return List.of();
    }

    @Override
    public List<Statement> getBranchTargets() {
        return List.of(getTarget());
    }

    @Override
    public List<RWCell<Statement>> getBranchTargetsCells() {
        return List.of(getTargetCell());
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }
}
