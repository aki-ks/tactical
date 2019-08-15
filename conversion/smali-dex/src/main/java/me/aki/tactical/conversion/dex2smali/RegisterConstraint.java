package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.conversion.dex2smali.provider.InstructionProvider;
import me.aki.tactical.conversion.dex2smali.provider.RegisterCell;
import me.aki.tactical.dex.Register;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.List;

/**
 * Require that a list of registers is stored ordered as a sequence in the generated smali code.
 *
 * If this is the case, range instructions such as "filled-new-array/range" can reference those registers.
 */
public class RegisterConstraint {
    /**
     * Instruction that requires the constraint.
     */
    private final InstructionProvider<? extends Instruction> instruction;

    /**
     * The cell for the first register of the range
     */
    private final RegisterCell firstRegisterCell;

    private List<Register> registers;

    public RegisterConstraint(InstructionProvider<? extends Instruction> instruction, List<Register> registers, RegisterCell firstRegisterCell) {
        this.instruction = instruction;
        this.registers = registers;
        this.firstRegisterCell = firstRegisterCell;
    }

    public InstructionProvider<? extends Instruction> getInstruction() {
        return instruction;
    }

    public RegisterCell getFirstRegisterCell() {
        return firstRegisterCell;
    }

    public List<Register> getRegisters() {
        return registers;
    }

    public void setRegisters(List<Register> registers) {
        this.registers = registers;
    }
}
