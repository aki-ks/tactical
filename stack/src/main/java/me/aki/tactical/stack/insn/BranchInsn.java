package me.aki.tactical.stack.insn;

import me.aki.tactical.core.util.Cell;

import java.util.List;

/**
 * Instruction that might jump to another locations in the code.
 */
public interface BranchInsn extends Instruction {
    /**
     * Get all instructions to which this instruction may branch.
     *
     * @return code locations where this instruction might branch.
     */
    List<Instruction> getBranchTargets();

    /**
     * Get cells of all instructions to which this instruction might branch.
     *
     * @return references to branch targets
     */
    List<Cell<Instruction>> getBranchTargetCells();
}
