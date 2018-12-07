package me.aki.tactical.dex.insn;

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
}
