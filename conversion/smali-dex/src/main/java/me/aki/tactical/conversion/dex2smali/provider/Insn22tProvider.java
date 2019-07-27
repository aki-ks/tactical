package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction22t;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22t;

import java.util.List;

public class Insn22tProvider implements InstructionProvider<Instruction22t> {
    private final Opcode opcode;
    private final RegisterCell registerA;
    private final RegisterCell registerB;
    private final AbstractOffsetCell offsetCell;

    public Insn22tProvider(Opcode opcode, Register registerA, Register registerB, Instruction target) {
        this.opcode = opcode;
        this.registerA = new RegisterCell(registerA);
        this.registerB = new RegisterCell(registerB);
        this.offsetCell = new OffsetCell(this, target);
    }

    public RegisterCell getRegisterACell() {
        return registerA;
    }

    public RegisterCell getRegisterBCell() {
        return registerB;
    }

    public AbstractOffsetCell getOffsetCell() {
        return offsetCell;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of(registerA, registerB);
    }

    @Override
    public List<AbstractOffsetCell> getOffsetCells() {
        return List.of(offsetCell);
    }

    @Override
    public Instruction22t newInstance() {
        return new ImmutableInstruction22t(opcode, registerA.get(), registerB.get(), offsetCell.get());
    }
}
