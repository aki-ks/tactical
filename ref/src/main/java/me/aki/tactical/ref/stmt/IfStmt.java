package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.condition.Condition;

import java.util.Set;

/**
 * Branch to another statement if a certain condition is true
 */
public class IfStmt implements BranchStmt {
    /**
     * Branch if this condition is true.
     */
    private Condition condition;

    /**
     * Branch to this statement.
     */
    private Statement target;

    public IfStmt(Condition condition, Statement target) {
        this.condition = condition;
        this.target = target;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
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
        return condition.getReadValueCells();
    }

    @Override
    public Set<Statement> getBranchTargets() {
        return Set.of(getTarget());
    }

    @Override
    public Set<RWCell<Statement>> getBranchTargetsCells() {
        return Set.of(getTargetCell());
    }
}
