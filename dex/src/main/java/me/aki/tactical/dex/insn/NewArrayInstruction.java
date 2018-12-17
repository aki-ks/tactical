package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Create an array of a certain type of a runtime defined size.
 */
public class NewArrayInstruction implements Instruction {
    /**
     * The type of array that gets created.
     */
    private ArrayType arrayType;

    /**
     * Register containing the size of the array.
     */
    private Register size;

    /**
     * Write the created array into this register.
     */
    private Register result;

    public NewArrayInstruction(ArrayType arrayType, Register size, Register result) {
        this.arrayType = arrayType;
        this.size = size;
        this.result = result;
    }

    public ArrayType getArrayType() {
        return arrayType;
    }

    public void setArrayType(ArrayType arrayType) {
        this.arrayType = arrayType;
    }

    public Register getSize() {
        return size;
    }

    public void setSize(Register size) {
        this.size = size;
    }

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(size);
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }
}
