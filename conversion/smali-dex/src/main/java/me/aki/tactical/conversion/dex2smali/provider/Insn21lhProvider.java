package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction21lh;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21lh;

import java.util.List;

public class Insn21lhProvider implements InstructionProvider<Instruction21lh> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final long literal;

    public Insn21lhProvider(Opcode opcode, Register registerA, long literal) {
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
    public List<OffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Format getFormat() {
        return Format.Format21lh;
    }

    @Override
    public Instruction21lh newInstance() {
        return new ImmutableInstruction21lh(opcode, registerA.get(), literal);
    }
}
