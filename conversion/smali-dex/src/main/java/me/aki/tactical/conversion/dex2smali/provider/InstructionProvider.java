package me.aki.tactical.conversion.dex2smali.provider;

import org.jf.dexlib2.iface.instruction.Instruction;

public interface InstructionProvider<I extends Instruction> {
    /**
     * Generate a new instance of a smali instruction.
     * This requires that all register and offset cells have been initialized.
     */
    I newInstance();
}
