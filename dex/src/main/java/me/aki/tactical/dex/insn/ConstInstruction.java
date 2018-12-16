package me.aki.tactical.dex.insn;

import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Instruction that stores a constant into an register
 */
public class ConstInstruction implements Instruction {
    /**
     * The constant that this instruction writed into a register.
     */
    private DexConstant constant;

    /**
     * Store the constant in this register.
     */
    private Register register;

    public ConstInstruction(DexConstant constant, Register register) {
        this.constant = constant;
        this.register = register;
    }

    public DexConstant getConstant() {
        return constant;
    }

    public void setConstant(DexConstant constant) {
        this.constant = constant;
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
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
