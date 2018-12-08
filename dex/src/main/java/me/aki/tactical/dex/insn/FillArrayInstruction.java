package me.aki.tactical.dex.insn;

import me.aki.tactical.core.constant.DexNumberConstant;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Fill an array with numeric constants.
 */
public class FillArrayInstruction implements Instruction {
    /**
     * Register containing the array that should be filled.
     */
    private Register array;

    /**
     * The numbers that should be stored in the array.
     */
    private List<DexNumberConstant> values;

    public FillArrayInstruction(Register array, List<DexNumberConstant> values) {
        this.array = array;
        this.values = values;
    }

    public Register getArray() {
        return array;
    }

    public void setArray(Register array) {
        this.array = array;
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
