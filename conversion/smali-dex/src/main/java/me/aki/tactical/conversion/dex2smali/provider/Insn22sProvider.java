package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction22s;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22s;

import java.util.List;

public class Insn22sProvider implements InstructionProvider<Instruction22s> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final RegisterCell registerB;
    private final short literal;

    public Insn22sProvider(Opcode opcode, Register registerA, Register registerB, short literal) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.registerB = new RegisterCell(registerB);
        this.literal = literal;
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
    public List<AbstractOffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Format getFormat() {
        return Format.Format22s;
    }

    @Override
    public Instruction22s newInstance() {
        return new ImmutableInstruction22s(opcode, registerA.get(), registerB.get(), literal);
    }
}
