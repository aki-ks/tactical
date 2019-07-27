package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.insn.Instruction;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10t;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction20t;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction30t;

public class GotoInsnProvider implements InstructionProvider<OffsetInstruction> {
    private AbstractOffsetCell offsetCell;

    public GotoInsnProvider(Instruction target) {
        this.offsetCell = new OffsetCell(this, target);
    }

    public AbstractOffsetCell getOffsetCell() {
        return offsetCell;
    }

    @Override
    public OffsetInstruction newInstance() {
        int offset = offsetCell.get();
        if (-128 <= offset && offset <= 127) {
            return new ImmutableInstruction10t(Opcode.GOTO, offset);
        } else if (-32768 <= offset && offset <= 32767) {
            return new ImmutableInstruction20t(Opcode.GOTO_16, offset);
        } else {
            return new ImmutableInstruction30t(Opcode.GOTO_32, offset);
        }
    }
}
