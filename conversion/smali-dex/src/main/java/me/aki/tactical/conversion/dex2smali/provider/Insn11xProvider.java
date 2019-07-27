package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction11x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11x;

import java.util.List;

public class Insn11xProvider implements InstructionProvider<Instruction11x> {
    private final Opcode opcode;
    private final RegisterCell registerA;

    public Insn11xProvider(Opcode opcode, Register registerA) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
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
    public Instruction11x newInstance() {
        return new ImmutableInstruction11x(opcode, registerA.get());
    }
}
