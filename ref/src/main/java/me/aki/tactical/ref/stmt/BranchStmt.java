package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Statement;

import java.util.List;

/**
 * Instruction that may branch to other instructions within the method.
 */
public interface BranchStmt extends Statement {
    public List<Cell<Statement>> getBranchTargets();
}
