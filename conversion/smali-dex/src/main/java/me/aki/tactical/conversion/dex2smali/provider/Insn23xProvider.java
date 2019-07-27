package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction23x;

import java.util.List;

public class Insn23xProvider implements InstructionProvider<Instruction23x> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final RegisterCell registerB;
    private final RegisterCell registerC;

    public Insn23xProvider(Opcode opcode, Register registerA, Register registerB, Register registerC) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.registerB = new RegisterCell(registerB);
        this.registerC = new RegisterCell(registerC);
    }

    public RegisterCell getRegisterACell() {
        return registerA;
    }

    public RegisterCell getRegisterBCell() {
        return registerB;
    }

    public RegisterCell getRegisterCCell() {
        return registerC;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of(registerA, registerB, registerC);
    }

    @Override
    public List<AbstractOffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Instruction23x newInstance() {
        return new ImmutableInstruction23x(opcode, registerA.get(), registerB.get(), registerC.get());
    }
}
