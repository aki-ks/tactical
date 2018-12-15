package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;
import me.aki.tactical.dex.utils.AbstractDexInsnWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Collect events from visitors where instructions references and registers are in smali-dexlib2
 * representation into tactical dex instructions.
 */
public class SmaliDexInsnWriter extends AbstractDexInsnWriter<org.jf.dexlib2.iface.instruction.Instruction, Integer> {
    private final List<Register> registers;
    private final Map<org.jf.dexlib2.iface.instruction.Instruction, Collection<Cell<Instruction>>> instructionRefs;

    public SmaliDexInsnWriter(List<Register> registers, Map<org.jf.dexlib2.iface.instruction.Instruction, Collection<Cell<Instruction>>> instructionRefs) {
        this.registers = registers;
        this.instructionRefs = instructionRefs;
    }

    @Override
    public Register convertRegister(Integer register) {
        return registers.get(register);
    }

    @Override
    public void registerReference(org.jf.dexlib2.iface.instruction.Instruction instruction, Cell<Instruction> cell) {
        Collection<Cell<Instruction>> cells = instructionRefs.computeIfAbsent(instruction, x -> new ArrayList<>());
        cells.add(cell);
    }
}
