package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.condition.Condition;

import java.util.List;

/**
 * Branch to another statement if a certain condition is true
 */
public class IfStmt implements BranchStmt {
    /**
     * Branch if this location is true.
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

    public Cell<Statement> getTargetCell() {
        return Cell.of(this::getTarget, this::setTarget, Statement.class);
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return condition.getReferencedValues();
    }

    @Override
    public List<Statement> getBranchTargets() {
        return List.of(getTarget());
    }

    @Override
    public List<Cell<Statement>> getBranchTargetsCells() {
        return List.of(getTargetCell());
    }
}
