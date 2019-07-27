package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction12x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction12x;

public class Insn12xProvider implements InstructionProvider<Instruction12x> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final RegisterCell registerB;

    public Insn12xProvider(Opcode opcode, Register registerA, Register registerB) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.registerB = new RegisterCell(registerB);
    }

    public RegisterCell getRegisterACell() {
        return registerA;
    }

    public RegisterCell getRegisterBCell() {
        return registerB;
    }

    @Override
    public Instruction12x newInstance() {
        return new ImmutableInstruction12x(opcode, registerA.get(), registerB.get());
    }
}
