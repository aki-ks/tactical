package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Insn35cProvider implements InstructionProvider<Instruction35c> {
    private final Opcode opcode;
    private final int registerCount;
    private final RegisterCell registerC;
    private final RegisterCell registerD;
    private final RegisterCell registerE;
    private final RegisterCell registerF;
    private final RegisterCell registerG;
    private final Reference reference;

    public Insn35cProvider(Opcode opcode, int registerCount, Register registerC, Register registerD, Register registerE, Register registerF, Register registerG, Reference reference) {
        this.opcode = opcode;
        this.registerCount = registerCount;
        this.registerC = new RegisterCell(registerC);
        this.registerD = new RegisterCell(registerD);
        this.registerE = new RegisterCell(registerE);
        this.registerF = new RegisterCell(registerF);
        this.registerG = new RegisterCell(registerG);
        this.reference = reference;
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

    @Override
    public List<RegisterCell> getRegisterCells() {
        return Stream.of(registerC, registerD, registerE, registerF, registerG)
                .limit(registerCount)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<AbstractOffsetCell> getOffsetCells() {
        return List.of();
    }

    @Override
    public Instruction35c newInstance() {
        return new ImmutableInstruction35c(opcode, registerCount, registerC.get(), registerD.get(), registerE.get(), registerF.get(), registerG.get(), reference);
    }
}
