package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.LinkedInsertList;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Several caches for the instructions of a method.
 */
public class InstructionIndex {
    private InsertList<Instruction> instructions;

    /**
     * A map from code-unit indices to the corresponding instruction.
     */
    private final Instruction[] instructionByCodeUnit;

    /**
     * Map instructions to the index of their first code unit.
     */
    private final Map<Instruction, Integer> codeUnitByInstruction;

    public InstructionIndex(Iterable<? extends Instruction> instructions) {
        this.instructions = new LinkedInsertList<>(instructions);

        this.codeUnitByInstruction = new HashMap<>();
        int addressOffset = 0;
        for (Instruction instruction : this.instructions) {
            this.codeUnitByInstruction.put(instruction, addressOffset);
            addressOffset += instruction.getCodeUnits();
        }

        List<Instruction> builder = new ArrayList<>();
        for (Instruction instruction : this.instructions) {
            builder.addAll(Collections.nCopies(instruction.getCodeUnits(), instruction));
        }
        this.instructionByCodeUnit = builder.toArray(new Instruction[0]);
    }

    public InsertList<Instruction> getInstructions() {
        return instructions;
    }

    /**
     * Get the instruction that is represented by a certain code point.
     *
     * @param codeUnit a code unit of the method
     * @return the corresponding instruction
     */
    public Instruction getInstructionByCodeUnit(int codeUnit) {
        return instructionByCodeUnit[codeUnit];
    }

    /**
     * Get the offset of a instruction in amount of code units.
     *
     * @param instruction the instruction
     * @return the offset of the instruction in the method
     */
    public int getCodeUnit(Instruction instruction) {
        return codeUnitByInstruction.get(instruction);
    }

    /**
     * Get a instruction by an offset relative to another instruction.
     *
     * @param instruction the offset is relative to this instruction
     * @param offset the offset in code units
     * @return the instruction at the offset
     */
    public Instruction getOffsetInstruction(Instruction instruction, int offset) {
        int baseOffset = getCodeUnit(instruction);
        return getInstructionByCodeUnit(baseOffset + offset);
    }
}
