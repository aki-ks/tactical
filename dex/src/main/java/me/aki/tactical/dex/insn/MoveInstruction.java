package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Move a value from one {@link Register} to another one.
 */
public class MoveInstruction implements Instruction {
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

    public MoveInstruction(DexType type, Register from, Register to) {
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

    public RWCell<Register> getFromCell() {
        return RWCell.of(this::getFrom, this::setFrom, Register.class);
    }

    public Register getTo() {
        return to;
    }

    public void setTo(Register to) {
        this.to = to;
    }

    public RWCell<Register> getToCell() {
        return RWCell.of(this::getTo, this::setTo, Register.class);
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(from);
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of(getFromCell());
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(to);
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.of(getToCell());
    }
}
