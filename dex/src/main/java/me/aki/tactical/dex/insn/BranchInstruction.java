package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.Cell;

import java.util.List;

/**
 * A instruction that might branch to another location in code.
 */
public interface BranchInstruction extends Instruction {
    /**
     * Get all instructions to which this instruction might branch.
     *
     * @return branch targets of this instruction
     */
    List<Instruction> getBranchTargets();

    /**
     * Get cells of all instructions to which this instruction might branch.
     *
     * @return cells for all branch targets of this instruction
     */
    List<Cell<Instruction>> getBranchTargetCells();
}
