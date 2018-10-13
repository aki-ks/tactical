package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Unconditional branch to another statement.
 */
public class GotoStatement implements BranchStatement {
    /**
     * Branch to this statement.
     */
    private Statement location;

    public GotoStatement(Statement location) {
        this.location = location;
    }

    @Override
    public List<Statement> getBranchTargets() {
        return List.of(location);
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }
}
