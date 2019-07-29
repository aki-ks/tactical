package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.conversion.dex2smali.provider.*;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.LinkedInsertList;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.*;

/**
 * Utility that calculated and sets the offset of all {@link OffsetCell OffsetCells} in a list of {@link InstructionProvider InstructionProviders}.
 *
 * This is a complicated task since some instructions change their size if their {@link OffsetCell} gets a large value assigned.
 * This can invalidate already computed offset.
 */
public class CodeUnitComputation {
    private final InsertList<InstructionProvider<?>> instructions;

    /**
     * Store for each instruction which cells must get updated if they change their size.
     */
    private final Map<InstructionProvider<?>, Set<OffsetCell>> affectionMap = new HashMap<>();

    public CodeUnitComputation(List<InstructionProvider<?>> instructions) {
        this.instructions = new LinkedInsertList<>(instructions);
        this.instructions.forEach(this::addToAffectionMap);
    }

    Map<InstructionProvider<?>, Set<OffsetCell>> getAffectionMap() {
        return affectionMap;
    }

    /**
     * Insert all OffsetCells of an {@link InstructionProvider} into the {@link CodeUnitComputation#affectionMap} map.
     *
     * This is done by walking from the instruction passed as parameter till the end of the method.
     * If we thereby pass by a targeted instruction of an OffsetCell, we know that the cell is affected
     * if any of the instructions we've seen so far changes in size.
     *
     * This procedure must also be done walking to the beginning of the instruction.
     * This method does both in parallel and quits if all cells have been processed.
     *
     * @param instruction
     */
    private void addToAffectionMap(InstructionProvider<?> instruction) {
        Set<OffsetCell> notYetAddedCells = new HashSet<>(instruction.getOffsetCells());

        InstructionProvider<?> forwardCursor = instruction;
        InstructionProvider<?> backwardCursor = instruction;
        Set<InstructionProvider<?>> forwardPath = new HashSet<>();
        Set<InstructionProvider<?>> backwardPath = new HashSet<>();

        while (!notYetAddedCells.isEmpty()) {
            if (forwardCursor == null && backwardCursor == null) {
                // We've visited all instructions in the method but have not found the targeted instructions of all OffsetCells
                throw new IllegalStateException("A cell points an instruction that is not contained in method");
            }

            if (forwardCursor != null) {
                addToMapIfAffected(notYetAddedCells, forwardCursor, forwardPath);
                forwardPath.add(forwardCursor);
            }

            forwardCursor = forwardCursor == null ? null : instructions.getNext(forwardCursor);
            backwardCursor = backwardCursor == null ? null : instructions.getPrevious(backwardCursor);

            if (backwardCursor != null) {
                backwardPath.add(backwardCursor);
                addToMapIfAffected(notYetAddedCells, backwardCursor, backwardPath);
            }
        }
    }

    /**
     *
     * @param cells cells that have to be processed
     * @param cursor an instruction
     * @param path a set containing all instruction between the {@link OffsetCell#getRelativeTo()} instruction of the cells and the cursor
     */
    private void addToMapIfAffected(Set<OffsetCell> cells, InstructionProvider<?> cursor, Set<InstructionProvider<?>> path) {
        Iterator<OffsetCell> cellIter = cells.iterator();
        while (cellIter.hasNext()) {
            OffsetCell cell = cellIter.next();

            if (cell.getTarget() == cursor) {
                setCellAffectedByInstructions(path, cell);
                cellIter.remove();
            }
        }
    }

    /**
     * Mark a cell as affected by a set of instructions.
     *
     * @param instructions instructions that affect the cell
     * @param cell the cell that gets affected
     */
    private void setCellAffectedByInstructions(Set<InstructionProvider<?>> instructions, OffsetCell cell) {
        for (InstructionProvider<?> affectedInsn : instructions) {
            affectionMap.computeIfAbsent(affectedInsn, x -> new HashSet<>()).add(cell);
        }
    }

    public void updateOffsets() {
        instructions.forEach(this::updateOffset);
    }

    private void updateOffset(InstructionProvider<?> instruction) {
        Format formatBeforeUpdate = instruction.getFormat();

        for (OffsetCell offsetCell : instruction.getOffsetCells()) {
            int offset = calculateOffset(instruction, offsetCell.getTarget());
            offsetCell.set(offset);
        }

        Format formatAfterUpdate = instruction.getFormat();

        if (formatBeforeUpdate.size != formatAfterUpdate.size) {
            Set<OffsetCell> affectedCells = getAffectionMap().getOrDefault(instruction, Set.of());
            for (OffsetCell affectedCell : affectedCells) {
                updateOffset(affectedCell.getTarget());
            }
        }
    }

    /**
     * Calculate the offset between two instruction in code units.
     *
     * @param relativeTo calculate the offset relative to this instruction
     * @param target calculate the offset of this instruction
     * @return the offset between the two instructions
     */
    int calculateOffset(InstructionProvider<?> relativeTo, InstructionProvider<?> target) {
        int forwardOffset = 0;
        int backwardOffset = 0;
        InstructionProvider<?> forwardCursor = relativeTo;
        InstructionProvider<?> backwardCursor = relativeTo;

        while (forwardCursor != null || backwardCursor != null) {
            if (forwardCursor != null) {
                if (forwardCursor == target) {
                    // divide by two to get the size in code units
                    return forwardOffset / 2;
                }

                forwardOffset += forwardCursor.getFormat().size;
            }

            forwardCursor = forwardCursor == null ? null : instructions.getNext(forwardCursor);
            backwardCursor = backwardCursor == null ? null : instructions.getPrevious(backwardCursor);

            if (backwardCursor != null) {
                backwardOffset -= backwardCursor.getFormat().size;

                if (backwardCursor == target) {
                    // divide by two to get the size in code units
                    return backwardOffset / 2;
                }
            }
        }

        throw new IllegalStateException("A cell points an relativeTo that is not contained in method");
    }
}
