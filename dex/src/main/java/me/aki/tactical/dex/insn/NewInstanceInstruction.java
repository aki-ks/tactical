package me.aki.tactical.dex.insn;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Create a new instance of a certain class and store it in a {@link Register}.
 */
public class NewInstanceInstruction implements Instruction {
    /**
     * Class of the object to be created.
     */
    private Path type;

    /**
     * The created object gets stored in this register.
     */
    private Register result;

    public NewInstanceInstruction(Path type, Register result) {
        this.type = type;
        this.result = result;
    }

    public Path getType() {
        return type;
    }

    public void setType(Path type) {
        this.type = type;
    }

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    public RWCell<Register> getResultCell() {
        return RWCell.of(this::getResult, this::setResult, Register.class);
    }

    @Override
    public Set<Register> getReadRegisters() {
        return Set.of();
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return Set.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.of(getResultCell());
    }
}
