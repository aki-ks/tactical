package me.aki.tactical.conversion.dex2smali.provider;

import org.jf.dexlib2.iface.instruction.Instruction;

/**
 * An {@link AbstractOffsetCell} where the pointed at instructions is represented as an {@link InstructionProvider}.
 *
 * This cell is necessary if we want to point at a just generated payload instruction.
 */
public class ProviderOffsetCell extends AbstractOffsetCell {
    private final InstructionProvider<? extends Instruction> target;

    public ProviderOffsetCell(InstructionProvider<? extends Instruction> relativeTo, InstructionProvider<? extends Instruction> target) {
        super(relativeTo);
        this.target = target;
    }

    public InstructionProvider<? extends Instruction> getTarget() {
        return target;
    }
}
