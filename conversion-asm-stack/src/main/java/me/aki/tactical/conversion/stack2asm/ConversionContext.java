package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.insn.Instruction;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ConversionContext {
    private final StackBody body;
    private final List<StackLocal> locals;
    private final Map<Instruction, List<Cell<LabelNode>>> convertedLabels = new IdentityHashMap<>();

    public ConversionContext(StackBody body) {
        this.body = body;
        this.locals = new ArrayList<>(body.getLocals());
    }

    /**
     * Get the unique index of a local.
     * If the local in not contained within {@link StackBody#getLocals()},
     * then a new index is assigned for that local.
     *
     * @param local whose index we want
     * @return local index
     */
    public int getLocalIndex(StackLocal local) {
        int index = locals.indexOf(local);
        if (index < 0) {
            index = locals.size();
            locals.add(local);
        }
        return index;
    }

    public void registerLabel(Instruction target, Cell<LabelNode> labelCell) {
        List<Cell<LabelNode>> cells = convertedLabels.computeIfAbsent(target, x -> new ArrayList<>());
        cells.add(labelCell);
    }

    public Map<Instruction, List<Cell<LabelNode>>> getConvertedLabels() {
        return convertedLabels;
    }
}
