package me.aki.tactical.dex.insn;

import me.aki.tactical.core.constant.DexNumberConstant;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Create a new array filled with numeric constants.
 *
 * The created array is required by a {@link MoveResultInstruction} that follows this instruction.
 */
public class NewConstantFilledArrayInstruction implements Instruction {
    /**
     * The numbers contained within the new array.
     */
    private List<DexNumberConstant> values;

    public NewConstantFilledArrayInstruction(List<DexNumberConstant> values) {
        this.values = values;
    }

    public List<DexNumberConstant> getValues() {
        return values;
    }

    public void setValues(List<DexNumberConstant> values) {
        this.values = values;
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
