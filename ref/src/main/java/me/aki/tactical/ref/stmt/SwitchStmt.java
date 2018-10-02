package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
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
    private LinkedHashMap<Integer, Statement> branchTable = new LinkedHashMap<>();

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

    public Cell<Expression> getValueCell() {
        return Cell.of(this::getValue, this::setValue, Expression.class);
    }

    public LinkedHashMap<Integer, Statement> getBranchTable() {
        return branchTable;
    }

    public void setBranchTable(LinkedHashMap<Integer, Statement> branchTable) {
        this.branchTable = branchTable;
    }

    public List<Cell<Statement>> getBranchTableCells() {
        return this.branchTable.keySet().stream()
                .map(key -> Cell.ofMap(key, this.branchTable, Statement.class))
                .collect(Collectors.toList());
    }

    public Statement getDefaultTarget() {
        return defaultTarget;
    }

    public void setDefaultTarget(Statement defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public Cell<Statement> getDefaultTargetCell() {
        return Cell.of(this::getDefaultTarget, this::setDefaultTarget, Statement.class);
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getValueCell());
    }

    @Override
    public List<Cell<Statement>> getBranchTargets() {
        List<Cell<Statement>> cells = new ArrayList<>();
        cells.addAll(getBranchTableCells());
        cells.add(getDefaultTargetCell());
        return Collections.unmodifiableList(cells);
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }
}
