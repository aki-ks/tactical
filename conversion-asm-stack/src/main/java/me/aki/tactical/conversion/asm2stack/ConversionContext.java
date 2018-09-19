package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.insn.Instruction;
import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversionContext {
    private List<Local> locals = new ArrayList<>();
    private Map<Label, List<Cell<Instruction>>> labelCells = new HashMap<>();

    public Local getLocal(int index) {
        return this.locals.get(index);
    }

    public void registerInsnCell(Label label, Cell<Instruction> insnCell) {
        List<Cell<Instruction>> cells = this.labelCells.computeIfAbsent(label, x -> new ArrayList<>());
        cells.add(insnCell);
    }

    public List<Local> getLocals() {
        return locals;
    }

    public Map<Label, List<Cell<Instruction>>> getLabelCells() {
        return labelCells;
    }
}
