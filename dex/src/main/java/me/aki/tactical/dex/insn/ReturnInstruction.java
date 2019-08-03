package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Return a value from a method.
 *
 * @see ReturnVoidInstruction to return from 'void' methods
 */
public class ReturnInstruction implements Instruction {
    /**
     * Type of value to be returned.
     */
    private DexType type;

    /**
     * Register that contains the value to be returned.
     */
    private Register register;

    public ReturnInstruction(DexType type, Register register) {
        this.type = type;
        this.register = register;
    }

    public DexType getType() {
        return type;
    }

    public void setType(DexType type) {
        this.type = type;
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
        return List.of(register);
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of(getRegisterCell());
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.empty();
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }
}
