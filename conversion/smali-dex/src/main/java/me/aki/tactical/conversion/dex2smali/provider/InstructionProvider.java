package me.aki.tactical.conversion.dex2smali.provider;

import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.List;

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
    List<AbstractOffsetCell> getOffsetCells();

    /**
     * Generate a new instance of a smali instruction.
     * This requires that all register and offset cells have been initialized.
     */
    I newInstance();
}
