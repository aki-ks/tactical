package me.aki.tactical.dex.insn;

import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Instruction that stores a constant into an register
 */
public class ConstInstruction implements Instruction {
    /**
     * The constant that this instruction written into the register.
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

    public RWCell<Register> getRegisterCell() {
        return RWCell.of(this::getRegister, this::setRegister, Register.class);
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of();
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(register);
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.of(getRegisterCell());
    }
}
