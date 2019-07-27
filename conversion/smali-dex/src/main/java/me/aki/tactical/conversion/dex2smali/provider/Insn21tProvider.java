package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction21t;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21t;

public class Insn21tProvider implements InstructionProvider<Instruction21t> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final AbstractOffsetCell offsetCell;

    public Insn21tProvider(Opcode opcode, Register registerA, Instruction target) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.offsetCell = new OffsetCell(this, target);
    }

    public RegisterCell getRegisterACell() {
        return registerA;
    }

    public AbstractOffsetCell getOffsetCell() {
        return offsetCell;
    }

    @Override
    public Instruction21t newInstance() {
        return new ImmutableInstruction21t(opcode, registerA.get(), offsetCell.get());
    }
}
