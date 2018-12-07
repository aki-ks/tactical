package me.aki.tactical.dex;

import me.aki.tactical.core.Body;
import me.aki.tactical.dex.insn.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DexBody implements Body {
    /**
     * All registers of this method
     */
    private List<Register> registers;

    /**
     * The register that initially contains the this value
     */
    private Optional<Register> thisRegister;

    /**
     * The registers that initially contains the parameters values
     */
    private List<Register> parameterRegisters;

    /**
     * All instructions of this method.
     */
    private List<Instruction> instructions;

    /**
     * The try/catch blocks of this method that catch and handle exceptions.
     */
    private List<TryCatchBlock> tryCatchBlocks;

    public DexBody() {
        this(new ArrayList<>(), Optional.empty(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public DexBody(List<Register> registers, Optional<Register> thisRegister, List<Register> parameterRegisters,
                   List<Instruction> instructions, List<TryCatchBlock> tryCatchBlocks) {
        this.registers = registers;
        this.thisRegister = thisRegister;
        this.parameterRegisters = parameterRegisters;
        this.instructions = instructions;
        this.tryCatchBlocks = tryCatchBlocks;
    }

    public List<Register> getRegisters() {
        return registers;
    }

    public void setRegisters(List<Register> registers) {
        this.registers = registers;
    }

    public Optional<Register> getThisRegister() {
        return thisRegister;
    }

    public void setThisRegister(Optional<Register> thisRegister) {
        this.thisRegister = thisRegister;
    }

    public List<Register> getParameterRegisters() {
        return parameterRegisters;
    }

    public void setParameterRegisters(List<Register> parameterRegisters) {
        this.parameterRegisters = parameterRegisters;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public List<TryCatchBlock> getTryCatchBlocks() {
        return tryCatchBlocks;
    }

    public void setTryCatchBlocks(List<TryCatchBlock> tryCatchBlocks) {
        this.tryCatchBlocks = tryCatchBlocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DexBody dexBody = (DexBody) o;
        return Objects.equals(registers, dexBody.registers) &&
                Objects.equals(thisRegister, dexBody.thisRegister) &&
                Objects.equals(parameterRegisters, dexBody.parameterRegisters) &&
                Objects.equals(instructions, dexBody.instructions) &&
                Objects.equals(tryCatchBlocks, dexBody.tryCatchBlocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registers, thisRegister, parameterRegisters, instructions, tryCatchBlocks);
    }
}
