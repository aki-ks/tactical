package me.aki.tactical.dex.utils;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;

/**
 * Visitor that collects all events into a list of {@link Instruction Instructions}.
 */
public class DexInsnWriter extends AbstractDexInsnWriter<Instruction, Register> {
    @Override
    public Register convertRegister(Register register) {
        return register;
    }

    @Override
    public void registerReference(Instruction instruction, RWCell<Instruction> insnCell) {
        insnCell.set(instruction);
    }
}
