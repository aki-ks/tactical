package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Return from a 'void' method.
 *
 * @see ReturnStatement to return from non-void methods.
 */
public class ReturnVoidStatement implements Statement {
    @Override
    public List<Register> getReadRegisters() {
        return List.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }
}
