package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Return a value from a method.
 *
 * @see ReturnVoidStatement to return from 'void' methods
 */
public class ReturnStatement implements Statement {
    /**
     * Type of value to be returned.
     */
    private DexType type;

    /**
     * Registed that contains the value to be returned.
     */
    private Register register;

    public ReturnStatement(DexType type, Register register) {
        this.type = type;
        this.register = register;
    }

    public DexType getType() {
        return type;
    }

    public void setType(DexType type) {
        this.type = type;
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(register);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }
}
