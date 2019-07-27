package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.dex.insn.Instruction;

/**
 * An {@link AbstractOffsetCell} where the pointed at instructions is represented as an {@link InstructionProvider}
 */
public class OffsetCell extends AbstractOffsetCell {
    /**
     * The cell should point at this instruction in smali representation.
     */
    private final Instruction target;

    public OffsetCell(InstructionProvider<?> relativeTo, Instruction target) {
        super(relativeTo);
        this.target = target;
    }

    public Instruction getTarget() {
        return target;
    }
}
