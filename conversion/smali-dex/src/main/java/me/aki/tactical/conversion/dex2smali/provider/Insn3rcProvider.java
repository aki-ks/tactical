package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction3rc;

import java.util.List;

public class Insn3rcProvider implements InstructionProvider<Instruction3rc> {
    private final Opcode opcode;
    private final RegisterCell startRegister;
    private final int registerCount;
    private final Reference reference;

    public Insn3rcProvider(Opcode opcode, Register startCell, int registerCount, Reference reference) {
        this.opcode = opcode;
        this.startRegister = new RegisterCell(startCell);
        this.registerCount = registerCount;
        this.reference = reference;
    }

    public RegisterCell getStartRegister() {
        return startRegister;
    }

    public int getRegisterCount() {
        return registerCount;
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
        return Format.Format3rc;
    }

    @Override
    public Instruction3rc newInstance() {
        return new ImmutableInstruction3rc(opcode, startRegister.get(), registerCount, reference);
    }
}
