package me.aki.tactical.dex.insn;

import me.aki.tactical.core.Path;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

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

    @Override
    public List<Register> getReadRegisters() {
        return List.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }
}
