package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22c;

import java.util.List;

public class Insn22cProvider implements InstructionProvider<Instruction22c> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final RegisterCell registerB;
    private final Reference reference;

    public Insn22cProvider(Opcode opcode, Register registerA, Register registerB, Reference reference) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.registerB = new RegisterCell(registerB);
        this.reference = reference;
    }

    public RegisterCell getRegisterACell() {
        return registerA;
    }

    public RegisterCell getRegisterBCell() {
        return registerB;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of(registerA, registerB);
    }

    @Override
    public List<OffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Format getFormat() {
        return Format.Format22c;
    }

    @Override
    public Instruction22c newInstance() {
        return new ImmutableInstruction22c(opcode, registerA.get(), registerB.get(), reference);
    }
}
