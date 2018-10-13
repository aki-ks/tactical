package me.aki.tactical.dex.statement;

import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Statement that stores a constant into an register
 */
public class ConstStatement implements Statement {
    private DexConstant constant;

    public ConstStatement(DexConstant constant) {
        this.constant = constant;
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
