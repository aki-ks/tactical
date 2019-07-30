package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction4rcc;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction4rcc;

import java.util.List;

public class Insn4rccProvider implements InstructionProvider<Instruction4rcc> {
    private final Opcode opcode;
    private final RegisterCell startRegister;
    private final int registerCount;
    private final Reference methodRef;
    private final Reference methodProto;

    public Insn4rccProvider(Opcode opcode, Register startRegister, int registerCount, Reference methodRef, Reference methodProto) {
        this.opcode = opcode;
        this.startRegister = new RegisterCell(startRegister);
        this.registerCount = registerCount;
        this.methodRef = methodRef;
        this.methodProto = methodProto;
    }

    public RegisterCell getStartRegisterCell() {
        return startRegister;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of(startRegister);
    }

    @Override
    public List<OffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Format getFormat() {
        return Format.Format4rcc;
    }

    @Override
    public Instruction4rcc newInstance() {
        return new ImmutableInstruction4rcc(opcode, startRegister.get(), registerCount, methodRef, methodProto);
    }
}
