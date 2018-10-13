package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.Register;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Compare a value from a register against a branch table and jump to the found or a default value.
 *
 * This statement represents the <tt>sparse-switch</tt> and <tt>packed-switch</tt> opcodes.
 */
public class SwitchStatement implements BranchStatement {
    /**
     * Compare this value against the branch table to get the branch location.
     */
    private Register value;

    /**
     * Map certain values to branch locations.
     */
    private LinkedHashMap<Integer, Statement> branchTable;

    /**
     * Branch to this location if the branchTable defined no target for the value.
     */
    private Statement defaultBranch;

    public SwitchStatement(Register value, LinkedHashMap<Integer, Statement> branchTable, Statement defaultBranch) {
        this.value = value;
        this.branchTable = branchTable;
        this.defaultBranch = defaultBranch;
    }

    public Register getValue() {
        return value;
    }

    public void setValue(Register value) {
        this.value = value;
    }

    public LinkedHashMap<Integer, Statement> getBranchTable() {
        return branchTable;
    }

    public void setBranchTable(LinkedHashMap<Integer, Statement> branchTable) {
        this.branchTable = branchTable;
    }

    public Statement getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(Statement defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    @Override
    public List<Statement> getBranchTargets() {
        return Stream.concat(branchTable.values().stream(), Stream.of(defaultBranch))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(value);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }
}
