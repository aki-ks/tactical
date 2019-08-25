package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.Optional;
import java.util.Set;

/**
 * Calculate the bitwise complement of an int or long.
 */
public class NotInstruction implements Instruction {
    /**
     * Type of value that this instruction negates.
     *
     * This should be {@link IntType#getInstance()} or {@link LongType#getInstance()}.
     */
    private PrimitiveType type;

    /**
     * Register that contains the value to be negated.
     */
    private Register value;

    /**
     * Store the negated value in this register
     */
    private Register result;

    public NotInstruction(PrimitiveType type, Register value, Register result) {
        setType(type);
        this.value = value;
        this.result = result;
    }

    public PrimitiveType getType() {
        return type;
    }

    public void setType(PrimitiveType type) {
        if (type instanceof IntLikeType || type instanceof LongType) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Expected an int or long type");
        }
    }

    public Register getValue() {
        return value;
    }

    public void setValue(Register value) {
        this.value = value;
    }

    public RWCell<Register> getValueCell() {
        return RWCell.of(this::getValue, this::setValue, Register.class);
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
        return Set.of(value);
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return Set.of(getValueCell());
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
