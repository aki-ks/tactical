package me.aki.tactical.conversion.ref2stack;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.insn.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversionContext {
    /**
     * Map ref locals to their equivalents in the stack body.
     */
    private final Map<RefLocal, StackLocal> localMap = new HashMap<>();

    /**
     * {@link Instruction} cells that should be assigned to the equivalent of a certain
     * {@link Statement} once the method is build.
     */
    private final Map<Statement, List<RWCell<Instruction>>> insnsRefs = new HashMap<>();

    /**
     * Register a reference to a instruction that will get updates once the whole body is build.
     *
     * @param target the statement that is reference
     * @param cell a cell that should later contain the equivalent instruction
     */
    public void registerInstructionReference(Statement target, RWCell<Instruction> cell) {
        List<RWCell<Instruction>> cells = insnsRefs.computeIfAbsent(target, x -> new ArrayList<>());
        cells.add(cell);
    }

    /**
     * Get a {@link StackLocal} that corresponds to a certain {@link RefLocal}.
     *
     * @param refLocal the ref local
     * @return the stack local
     */
    public StackLocal getStackLocal(RefLocal refLocal) {
        return this.localMap.get(refLocal);
    }

    public Map<RefLocal, StackLocal> getLocalMap() {
        return localMap;
    }

    public Map<Statement, List<RWCell<Instruction>>> getInstructionsRefs() {
        return insnsRefs;
    }
}
