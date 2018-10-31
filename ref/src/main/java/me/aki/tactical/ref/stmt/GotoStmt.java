package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;

import java.util.List;
import java.util.Objects;

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
    public List<Cell<Expression>> getReferencedValueCells() {
        return List.of();
    }

    @Override
    public List<Statement> getBranchTargets() {
        return List.of(getTarget());
    }

    @Override
    public List<Cell<Statement>> getBranchTargetsCells() {
        return List.of(getTargetCell());
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GotoStmt gotoStmt = (GotoStmt) o;
        return target == gotoStmt.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(target);
    }
}
