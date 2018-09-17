package me.aki.tactical.stack.insn;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public Instruction getDefaultLocation() {
        return defaultLocation;
    }

    public void setDefaultLocation(Instruction defaultLocation) {
        this.defaultLocation = defaultLocation;
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
}
