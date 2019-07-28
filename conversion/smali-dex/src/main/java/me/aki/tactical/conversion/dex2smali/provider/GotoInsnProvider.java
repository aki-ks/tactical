package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.dex.insn.Instruction;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10t;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction20t;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction30t;

import java.util.List;
import java.util.Set;

public class GotoInsnProvider implements InstructionProvider<OffsetInstruction> {
    private AbstractOffsetCell offsetCell;

    public GotoInsnProvider(Instruction target) {
        this.offsetCell = new OffsetCell(this, target);
    }

    public AbstractOffsetCell getOffsetCell() {
        return offsetCell;
    }

    @Override
    public List<RegisterCell> getRegisterCells() {
        return List.of();
    }

    @Override
    public List<AbstractOffsetCell> getOffsetCells() {
        return List.of(offsetCell);
    }

    @Override
    public Format getFormat() {
        int offset = offsetCell.get();
        return Byte.MIN_VALUE <= offset && offset <= Byte.MAX_VALUE ? Format.Format10t :
                Short.MIN_VALUE <= offset && offset <= Short.MAX_VALUE ? Format.Format20t :
                Format.Format30t;
    }

    @Override
    public Set<Format> getPossibleFormats() {
        return Set.of(Format.Format10t, Format.Format20t, Format.Format30t);
    }

    @Override
    public OffsetInstruction newInstance() {
        int offset = offsetCell.get();
        switch (getFormat()) {
            case Format10t: return new ImmutableInstruction10t(Opcode.GOTO, offset);
            case Format20t: return new ImmutableInstruction20t(Opcode.GOTO_16, offset);
            case Format30t: return new ImmutableInstruction30t(Opcode.GOTO_32, offset);
            default: return DexUtils.unreachable();
        }
    }
}
