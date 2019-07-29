package me.aki.tactical.conversion.dex2smali.provider;

import org.jf.dexlib2.Format;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.List;
import java.util.Set;

public interface InstructionProvider<I extends Instruction> {
    /**
     * Get all offset cells used to produce the instruction.
     *
     * @return unmodifiable list of all register cells
     */
    List<RegisterCell> getRegisterCells();

    /**
     * Get all offset cells used to produce the instruction.
     *
     * @return unmodifiable list of all offset cells
     */
    List<OffsetCell> getOffsetCells();

    /**
     * Get the format of the instruction that gets emitted by this provider in its current state.
     *
     * @return format of the produced instruction
     */
    Format getFormat();

    /**
     * All instructions format that may be possible produced by this instruction provider.
     *
     * @return all formats possible produced by this InstructionProvider
     */
    default Set<Format> getPossibleFormats() {
        return Set.of(getFormat());
    }

    /**
     * Generate a new instance of a smali instruction.
     * This requires that all register and offset cells have been initialized.
     */
    I newInstance();
}
