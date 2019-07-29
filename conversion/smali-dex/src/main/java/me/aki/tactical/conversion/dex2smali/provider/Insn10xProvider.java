package me.aki.tactical.conversion.dex2smali.provider;

import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction10x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;

import java.util.List;

public class Insn10xProvider implements InstructionProvider<Instruction10x> {
    private final Opcode opcode;

    public Insn10xProvider(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of();
    }

    @Override
    public List<OffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Format getFormat() {
        return Format.Format10x;
    }

    @Override
    public Instruction10x newInstance() {
        return new ImmutableInstruction10x(opcode);
    }
}
