package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.insn.Instruction;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversionContext {
    private List<Local> locals = new ArrayList<>();

    /**
     * Instruction cells that should be assigned to the converted instruction that
     * <b>succeeds</b> the corresponding {@link LabelNode}.
     */
    private Map<LabelNode, List<Cell<Instruction>>> forwardLabelCells = new HashMap<>();

    /**
     * Instruction cells that should be assigned to the converted instruction that
     * <b>precedes</b> the corresponding {@link LabelNode}.
     */
    private Map<LabelNode, List<Cell<Instruction>>> backwardLabelCells = new HashMap<>();

    public Local getLocal(int index) {
        return this.locals.get(index);
    }

    public void registerForwardInsnCell(LabelNode label, Cell<Instruction> insnCell) {
        this.forwardLabelCells.computeIfAbsent(label, x -> new ArrayList<>()).add(insnCell);
    }

    public void registerBackwardInsnCell(LabelNode label, Cell<Instruction> insnCell) {
        this.backwardLabelCells.computeIfAbsent(label, x -> new ArrayList<>()).add(insnCell);
    }

    public List<Local> getLocals() {
        return locals;
    }

    public Map<LabelNode, List<Cell<Instruction>>> getForwardLabelCells() {
        return forwardLabelCells;
    }

    public Map<LabelNode, List<Cell<Instruction>>> getBackwardLabelCells() {
        return backwardLabelCells;
    }
}
