package me.aki.tactical.dex.insn;

import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;

/**
 * Check whether a value is of a certain type and store the result in a {@link Register}.
 */
public class InstanceOfInstruction implements Instruction {
    /**
     * Check whether a value is of this type.
     */
    private RefType type;

    /**
     * Value to be checked.
     */
    private Register value;

    /**
     * Store the result of the check in this register.
     *
     * Store <tt>1</tt> for <tt>true</tt> or <tt>0</tt> for <tt>false</tt>.
     */
    private Register result;

    public InstanceOfInstruction(RefType type, Register value, Register result) {
        this.type = type;
        this.value = value;
        this.result = result;
    }

    public RefType getType() {
        return type;
    }

    public void setType(RefType type) {
        this.type = type;
    }

    public Register getValue() {
        return value;
    }

    public void setValue(Register value) {
        this.value = value;
    }

    public RWCell<Register> getValueCell() {
        return RWCell.of(this::getValue, this::setValue, Register.class);
    }

    public Register getResult() {
        return result;
    }

    public void setResult(Register result) {
        this.result = result;
    }

    public RWCell<Register> getResultCell() {
        return RWCell.of(this::getResult, this::setResult, Register.class);
    }

    @Override
    public List<Register> getReadRegisters() {
        return List.of(value);
    }

    @Override
    public List<RWCell<Register>> getReadRegisterCells() {
        return List.of(getValueCell());
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.of(result);
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.of(getResultCell());
    }
}
