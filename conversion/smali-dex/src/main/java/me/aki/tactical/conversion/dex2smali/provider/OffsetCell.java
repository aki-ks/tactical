package me.aki.tactical.conversion.dex2smali.provider;

import me.aki.tactical.core.util.Either;
import me.aki.tactical.core.util.RWCell;
import org.jf.dexlib2.iface.instruction.Instruction;

/**
 * A RWCell that should contain the offset of an instruction in the converted smali method.
 */
public class OffsetCell extends RWCell.Heap<Integer> {
    /**
     * The offset should be calculated relative to this instruction.
     */
    private final InstructionProvider<? extends Instruction> relativeTo;

    /**
     * The cell should point at this instruction.
     * It should be an {@link InstructionProvider} but may initially be set to an {@link Instruction}.
     */
    private Either<me.aki.tactical.dex.insn.Instruction, InstructionProvider<? extends Instruction>> target;

    public OffsetCell(InstructionProvider<? extends Instruction> relativeTo, InstructionProvider<? extends Instruction> target) {
        super(Integer.class, 0);
        this.relativeTo = relativeTo;
        this.target = Either.right(target);
    }

    public OffsetCell(InstructionProvider<? extends Instruction> relativeTo, me.aki.tactical.dex.insn.Instruction target) {
        super(Integer.class, 0);
        this.relativeTo = relativeTo;
        this.target = Either.left(target);
    }

    public InstructionProvider<? extends Instruction> getRelativeTo() {
        return relativeTo;
    }

    public boolean isUnresolved() {
        return target.isLeft();
    }

    public me.aki.tactical.dex.insn.Instruction getUnresolvedTarget() {
        return target.getLeft();
    }

    public void resolveTarget(InstructionProvider<? extends Instruction> target) {
        if (this.target.isLeft()) {
            this.target = Either.right(target);
        } else {
            throw new IllegalStateException("The instructions has already been resolved");
        }
    }

    public InstructionProvider<? extends Instruction> getTarget() {
        return target.getRight();
    }
}
