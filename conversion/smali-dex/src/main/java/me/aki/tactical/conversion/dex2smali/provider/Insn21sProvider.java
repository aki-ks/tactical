package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction21s;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21s;

public class Insn21sProvider implements InstructionProvider<Instruction21s> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final int literal;

    public Insn21sProvider(Opcode opcode, Register registerA, int literal) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.literal = literal;
    }

    public RegisterCell getRegisterACell() {
        return registerA;
    }

    @Override
    public Instruction21s newInstance() {
        return new ImmutableInstruction21s(opcode, registerA.get(), literal);
    }
}
