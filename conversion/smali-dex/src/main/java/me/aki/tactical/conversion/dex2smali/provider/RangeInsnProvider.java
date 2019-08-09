package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.List;

public abstract class RangeInsnProvider<I extends Instruction> implements InstructionProvider<I> {
    private final RegisterCell startRegister;
    private final int registerCount;

    protected RangeInsnProvider(Register startRegister, int registerCount) {
        this.startRegister = new RegisterCell(startRegister);
        this.registerCount = registerCount;
    }

    public RegisterCell getStartRegisterCell() {
        return startRegister;
    }

    public int getRegisterCount() {
        return registerCount;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of(startRegister);
    }
}
