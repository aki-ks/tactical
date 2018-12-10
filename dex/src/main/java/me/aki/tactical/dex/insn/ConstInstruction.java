package me.aki.tactical.dex.insn;

import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Instruction that stores a constant into an register
 */
public class ConstInstruction implements Instruction {
    private DexConstant constant;

    public ConstInstruction(DexConstant constant) {
        this.constant = constant;
    }

    public DexConstant getConstant() {
        return constant;
    }

    public void setConstant(DexConstant constant) {
        this.constant = constant;
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
