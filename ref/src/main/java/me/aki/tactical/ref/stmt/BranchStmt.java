package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Statement;

import java.util.List;

/**
 * Instruction that may branch to other instructions within the method.
 */
public interface BranchStmt extends Statement {
    /**
     * Get all statements to which this instruction might branch.
     *
     * @return a list of branch targets
     */
    List<Statement> getBranchTargets();

    /**
     * Get references to all uses of branch targets.
     *
     * @return cells of all branch targets
     */
    List<Cell<Statement>> getBranchTargetsCells();
}
