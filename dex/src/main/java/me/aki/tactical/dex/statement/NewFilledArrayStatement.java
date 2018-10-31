package me.aki.tactical.dex.statement;

import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Create a new array filled with values from certain registers.
 *
 * The created array is required by a {@link MoveResultStatement} that follows this instruction.
 */
public class NewFilledArrayStatement implements Statement {
    /**
     * Type of the array to be created.
     */
    private ArrayType type;

    /**
     * The values from these registers get stored in the created array.
     */
    private List<Register> registers;

    public NewFilledArrayStatement(ArrayType type, List<Register> registers) {
        this.type = type;
        this.registers = registers;
    }

    public ArrayType getType() {
        return type;
    }

    public void setType(ArrayType type) {
        this.type = type;
    }

    public List<Register> getRegisters() {
        return registers;
    }

    public void setRegisters(List<Register> registers) {
        this.registers = registers;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.copyOf(this.registers);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }
}
