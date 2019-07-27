package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction31i;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction31i;

import java.util.List;

public class Insn31iProvider implements InstructionProvider<Instruction31i> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final int literal;

    public Insn31iProvider(Opcode opcode, Register registerA, int literal) {
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
    public Instruction31i newInstance() {
        return new ImmutableInstruction31i(opcode, registerA.get(), literal);
    }
}
