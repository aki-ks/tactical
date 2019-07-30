package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction45cc;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction45cc;

import java.util.List;

public class Insn45ccProvider implements InstructionProvider<Instruction45cc> {
    private final Opcode opcode;
    private final int registerCount;
    private final RegisterCell registerC;
    private final RegisterCell registerD;
    private final RegisterCell registerE;
    private final RegisterCell registerF;
    private final RegisterCell registerG;
    private final Reference reference;
    private final Reference reference2;

    public Insn45ccProvider(Opcode opcode, int registerCount, Register registerC, Register registerD, Register registerE, Register registerF, Register registerG, Reference reference, Reference reference2) {
        this.opcode = opcode;
        this.registerCount = registerCount;
        this.registerC = new RegisterCell(registerC);
        this.registerD = new RegisterCell(registerD);
        this.registerE = new RegisterCell(registerE);
        this.registerF = new RegisterCell(registerF);
        this.registerG = new RegisterCell(registerG);
        this.reference = reference;
        this.reference2 = reference2;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of(registerC, registerD, registerE, registerF, registerG);
    }

    @Override
    public List<OffsetCell> getOffsetCells() {
        return List.of();
    }

    public RegisterCell getRegisterCCell() {
        return registerC;
    }

    public RegisterCell getRegisterDCell() {
        return registerD;
    }

    public RegisterCell getRegisterECell() {
        return registerE;
    }

    public RegisterCell getRegisterFCell() {
        return registerF;
    }

    public RegisterCell getRegisterGCell() {
        return registerG;
    }

    public Reference getReference() {
        return reference;
    }

    public Reference getReference2() {
        return reference2;
    }

    @Override
    public Format getFormat() {
        return Format.Format45cc;
    }

    @Override
    public Instruction45cc newInstance() {
        return new ImmutableInstruction45cc(opcode, registerCount, registerC.get(), registerD.get(), registerE.get(), registerF.get(), registerG.get(), reference, reference2);
    }
}
