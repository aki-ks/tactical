package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.core.util.RWCell;

/**
 * A RWCell that should contain the offset of an instruction in the converted smali method.
 */
public abstract class AbstractOffsetCell extends RWCell.Heap<Integer> {
    /**
     * The offset should be calculated relative to this instruction.
     */
    private final InstructionProvider<?> relativeTo;

    public AbstractOffsetCell(InstructionProvider<?> relativeTo) {
        super(Integer.class, 0);
        this.relativeTo = relativeTo;
    }

    public InstructionProvider<?> getRelativeTo() {
        return relativeTo;
    }
}
