package me.aki.tactical.stack.insn;

import me.aki.tactical.core.util.RWCell;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private LinkedHashMap<Integer, Instruction> branchTable;

    /**
     * The default branch location if a popped value is not contained
     * in the {@link SwitchInsn#branchTable} map.
     */
    private Instruction defaultLocation;

    public SwitchInsn(LinkedHashMap<Integer, Instruction> branchTable, Instruction defaultLocation) {
        this.branchTable = branchTable;
        this.defaultLocation = defaultLocation;
    }

    public Map<Integer, Instruction> getBranchTable() {
        return branchTable;
    }

    public void setBranchTable(LinkedHashMap<Integer, Instruction> branchTable) {
        this.branchTable = branchTable;
    }

    public RWCell<Instruction> getBranchTableCell(int key) {
        return RWCell.ofMap(key, branchTable, Instruction.class);
    }

    public List<RWCell<Instruction>> getBranchTableCells() {
        return branchTable.entrySet().stream()
                .map(e -> RWCell.ofMap(e.getKey(), branchTable, Instruction.class))
                .collect(Collectors.toList());
    }

    public Instruction getDefaultLocation() {
        return defaultLocation;
    }

    public void setDefaultLocation(Instruction defaultLocation) {
        this.defaultLocation = defaultLocation;
    }

    public RWCell<Instruction> getDefaultLocationCell() {
        return RWCell.of(this::getDefaultLocation, this::setDefaultLocation, Instruction.class);
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
    public List<RWCell<Instruction>> getBranchTargetCells() {
        List<RWCell<Instruction>> cells = getBranchTableCells();
        cells.add(getDefaultLocationCell());
        return cells;
    }
}
