package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction21ih;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21ih;

import java.util.List;

public class Insn21ihProvider implements InstructionProvider<Instruction21ih> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final int literal;

    public Insn21ihProvider(Opcode opcode, Register registerA, int literal) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.literal = literal;
    }

    public RegisterCell getRegisterACell() {
        return registerA;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of(registerA);
    }

    @Override
    public List<AbstractOffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Format getFormat() {
        return Format.Format21ih;
    }

    @Override
    public Instruction21ih newInstance() {
        return new ImmutableInstruction21ih(opcode, registerA.get(), literal);
    }
}
