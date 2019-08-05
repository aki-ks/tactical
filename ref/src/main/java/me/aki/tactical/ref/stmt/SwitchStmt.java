package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compare an int value against a branch table or else branch to a default location.
 */
public class SwitchStmt implements BranchStmt {
    /**
     * Value that will be compared against the branch table.
     */
    private Expression value;

    /**
     * Table that maps integers to branch locations.
     */
    private LinkedHashMap<Integer, Statement> branchTable;

    /**
     * Branch target if the value is not contained within the branch table.
     */
    private Statement defaultTarget;

    public SwitchStmt(Expression value, LinkedHashMap<Integer, Statement> branchTable, Statement defaultTarget) {
        this.value = value;
        this.branchTable = branchTable;
        this.defaultTarget = defaultTarget;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public RWCell<Expression> getValueCell() {
        return RWCell.of(this::getValue, this::setValue, Expression.class);
    }

    public LinkedHashMap<Integer, Statement> getBranchTable() {
        return branchTable;
    }

    public void setBranchTable(LinkedHashMap<Integer, Statement> branchTable) {
        this.branchTable = branchTable;
    }

    public RWCell<Statement> getBranchTableCell(int key) {
        return RWCell.ofMap(key, branchTable, Statement.class);
    }

    public List<RWCell<Statement>> getBranchTableCells() {
        return this.branchTable.keySet().stream()
                .map(key -> RWCell.ofMap(key, this.branchTable, Statement.class))
                .collect(Collectors.toList());
    }

    public Statement getDefaultTarget() {
        return defaultTarget;
    }

    public void setDefaultTarget(Statement defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public RWCell<Statement> getDefaultTargetCell() {
        return RWCell.of(this::getDefaultTarget, this::setDefaultTarget, Statement.class);
    }

    @Override
    public List<RCell<Expression>> getReadValueCells() {
        return List.of(getValueCell());
    }

    @Override
    public List<Statement> getBranchTargets() {
        List<Statement> targets = new ArrayList<>(getBranchTable().values());
        targets.add(getDefaultTarget());
        return Collections.unmodifiableList(targets);
    }

    @Override
    public List<RWCell<Statement>> getBranchTargetsCells() {
        List<RWCell<Statement>> cells = new ArrayList<>(getBranchTableCells());
        cells.add(getDefaultTargetCell());
        return Collections.unmodifiableList(cells);
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }
}
