package me.aki.tactical.stack.insn;

import me.aki.tactical.core.util.Cell;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An int value is popped from the stack and checked against a table of branch locations.
 *
 * This instruction replaces the TABLE_SWITCH and LOOKUP_SWITCH opcode.
 */
public class SwitchInsn extends AbstractInstruction implements BranchInsn {
    /**
     * Mapping from int constants to their corresponding branch location.
     */
    private Map<Integer, Instruction> branchTable = new LinkedHashMap<>();

    /**
     * The default branch location if a popped value is not contained
     * in the {@link SwitchInsn#branchTable} map.
     */
    private Instruction defaultLocation;

    public SwitchInsn(Map<Integer, Instruction> branchTable, Instruction defaultLocation) {
        this.branchTable = branchTable;
        this.defaultLocation = defaultLocation;
    }

    public Map<Integer, Instruction> getBranchTable() {
        return branchTable;
    }

    public void setBranchTable(Map<Integer, Instruction> branchTable) {
        this.branchTable = branchTable;
    }

    public List<Cell<Instruction>> getBranchTableCells() {
        return branchTable.entrySet().stream()
                .map(e -> Cell.ofMap(e.getKey(), branchTable, Instruction.class))
                .collect(Collectors.toList());
    }

    public Instruction getDefaultLocation() {
        return defaultLocation;
    }

    public void setDefaultLocation(Instruction defaultLocation) {
        this.defaultLocation = defaultLocation;
    }

    public Cell<Instruction> getDefaultLocationCell() {
        return Cell.of(this::getDefaultLocation, this::setDefaultLocation, Instruction.class);
    }

    @Override
    public int getPushCount() {
        return 0;
    }

    @Override
    public int getPopCount() {
        return 1;
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }

    @Override
    public List<Instruction> getBranchTargets() {
        List<Instruction> instruction = new ArrayList<>(branchTable.values());
        instruction.add(getDefaultLocation());
        return instruction;
    }

    @Override
    public List<Cell<Instruction>> getBranchTargetCells() {
        List<Cell<Instruction>> cells = getBranchTableCells();
        cells.add(getDefaultLocationCell());
        return cells;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SwitchInsn that = (SwitchInsn) o;
        return Objects.equals(branchTable, that.branchTable) &&
                Objects.equals(defaultLocation, that.defaultLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), branchTable, defaultLocation);
    }
}
