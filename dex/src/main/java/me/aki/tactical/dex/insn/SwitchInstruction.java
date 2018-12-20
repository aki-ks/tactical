package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.dex.Register;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Compare a value from a register against a branch table and jump to the found or a default value.
 *
 * This instruction represents the <tt>sparse-switch</tt> and <tt>packed-switch</tt> opcodes.
 */
public class SwitchInstruction implements BranchInstruction {
    /**
     * Compare this value against the branch table to get the branch location.
     */
    private Register value;

    /**
     * Map certain values to branch locations.
     */
    private LinkedHashMap<Integer, Instruction> branchTable;

    public SwitchInstruction(Register value, LinkedHashMap<Integer, Instruction> branchTable) {
        this.value = value;
        this.branchTable = branchTable;
    }

    public Register getValue() {
        return value;
    }

    public void setValue(Register value) {
        this.value = value;
    }

    public LinkedHashMap<Integer, Instruction> getBranchTable() {
        return branchTable;
    }

    public void setBranchTable(LinkedHashMap<Integer, Instruction> branchTable) {
        this.branchTable = branchTable;
    }

    @Override
    public List<Instruction> getBranchTargets() {
        return List.copyOf(branchTable.values());
    }

    @Override
    public List<Cell<Instruction>> getBranchTargetCells() {
        return branchTable.keySet().stream()
                .map(key -> Cell.ofMap(key, branchTable, Instruction.class))
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
