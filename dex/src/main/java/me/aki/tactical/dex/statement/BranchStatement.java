package me.aki.tactical.dex.statement;

import java.util.List;

/**
 * A statement that might branch to another location in code.
 */
public interface BranchStatement extends Statement {
    /**
     * Get all statements to which this statement might branch.
     *
     * @return branch targets of this statement
     */
    List<Statement> getBranchTargets();
}
