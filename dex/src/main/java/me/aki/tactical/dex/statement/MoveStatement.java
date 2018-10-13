package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Move a value from one {@link Register} to another one.
 */
public class MoveStatement implements Statement {
    /**
     * Type of value to be moved.
     */
    private DexType type;

    /**
     * Where to read the value from.
     */
    private Register from;

    /**
     * Where to write the value to.
     */
    private Register to;

    public MoveStatement(DexType type, Register from, Register to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    public DexType getType() {
        return type;
    }

    public void setType(DexType type) {
        this.type = type;
    }

    public Register getFrom() {
        return from;
    }

    public void setFrom(Register from) {
        this.from = from;
    }

    public Register getTo() {
        return to;
    }

    public void setTo(Register to) {
        this.to = to;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(from);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(to);
    }
}
