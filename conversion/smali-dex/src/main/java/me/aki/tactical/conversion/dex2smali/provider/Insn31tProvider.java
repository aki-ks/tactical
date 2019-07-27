package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction31t;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction31t;

public class Insn31tProvider implements InstructionProvider<Instruction31t> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final AbstractOffsetCell offsetCell;

    public Insn31tProvider(Opcode opcode, Register registerA, InstructionProvider<? extends Instruction> target) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.offsetCell = new ProviderOffsetCell(this, target);
    }

    public RegisterCell getRegisterACell() {
        return registerA;
    }

    public AbstractOffsetCell getOffsetCell() {
        return offsetCell;
    }

    @Override
    public Instruction31t newInstance() {
        return new ImmutableInstruction31t(opcode, registerA.get(), offsetCell.get());
    }
}
