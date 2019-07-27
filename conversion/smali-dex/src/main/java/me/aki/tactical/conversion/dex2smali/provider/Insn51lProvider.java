package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction51l;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction51l;

public class Insn51lProvider implements InstructionProvider<Instruction51l> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final long literal;

    public Insn51lProvider(Opcode opcode, Register registerA, long literal) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.literal = literal;
    }

    public RegisterCell getRegisterA() {
        return registerA;
    }

    @Override
    public Instruction51l newInstance() {
        return new ImmutableInstruction51l(opcode, registerA.get(), literal);
    }
}
