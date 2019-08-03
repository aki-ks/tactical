package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create a new array filled with values from certain registers.
 *
 * The created array is required by a {@link MoveResultInstruction} that follows this instruction.
 */
public class NewFilledArrayInstruction implements Instruction {
    /**
     * Type of the array to be created.
     */
    private ArrayType type;

    /**
     * The values from these registers get stored in the created array.
     */
    private List<Register> registers;

    public NewFilledArrayInstruction(ArrayType type, List<Register> registers) {
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

    public List<RWCell<Register>> getRegisterCells() {
        return IntStream.range(0, this.registers.size())
                .mapToObj(index -> RWCell.ofList(this.registers, index, Register.class))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.copyOf(this.registers);
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return getRegisterCells();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.empty();
    }
}
