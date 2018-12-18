package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.LinkedInsertList;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Several caches for the instructions of a method.
 */
public class InstructionIndex {
    private InsertList<Instruction> instructions;

    /**
     * A map from code-unit indices to the corresponding instruction.
     */
    private final Instruction[] instructionByCodeUnit;

    public InstructionIndex(Iterable<? extends Instruction> instructions) {
        this.instructions = new LinkedInsertList<>(instructions);

        List<Instruction> builder = new ArrayList<>();
        for (Instruction instruction : instructions) {
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
}
