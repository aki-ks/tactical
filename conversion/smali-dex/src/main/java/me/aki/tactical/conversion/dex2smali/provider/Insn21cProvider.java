package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;

import java.util.List;

public class Insn21cProvider implements InstructionProvider<Instruction21c> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final Reference reference;

    public Insn21cProvider(Opcode opcode, Register registerA, Reference reference) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.reference = reference;
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
        return Format.Format21c;
    }

    @Override
    public Instruction21c newInstance() {
        return new ImmutableInstruction21c(opcode, registerA.get(), reference);
    }
}
