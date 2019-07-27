package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction11n;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11n;

import java.util.List;

public class Insn11nProvider implements InstructionProvider<Instruction11n> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final int literal;

    public Insn11nProvider(Opcode opcode, Register registerA, int literal) {
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
    public Instruction11n newInstance() {
        return new ImmutableInstruction11n(opcode, registerA.get(), literal);
    }
}
