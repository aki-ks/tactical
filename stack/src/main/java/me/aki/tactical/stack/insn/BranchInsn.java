package me.aki.tactical.stack.insn;

import me.aki.tactical.core.util.Cell;

import java.util.List;

/**
 * Instruction that might jump to another locations in the code.
 */
public interface BranchInsn extends Instruction {
    /**
     * Get references to all instructions referenced by this branch instruction.
     *
     * @return references to all referenced instructions
     */
    List<Cell<Instruction>> getInstructionCells();
}
