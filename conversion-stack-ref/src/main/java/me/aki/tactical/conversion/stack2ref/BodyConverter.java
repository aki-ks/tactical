package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.insn.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility that builds a {@link RefBody} from a {@link StackBody}.
 */
public class BodyConverter {
    private final StackBody stackBody;
    private final RefBody refBody;

    /**
     * Map locals in stack representation to the corresponding ref locals.
     */
    private final Map<StackLocal, RefLocal> localMap = new HashMap<>();

    /**
     * Cells that should be assigned to the statement corresponding to a certain instructions.
     */
    private final Map<Instruction, List<Cell<Statement>>> instructionReferences = new HashMap<>();

    public BodyConverter(StackBody stackBody) {
        this.stackBody = stackBody;
        this.refBody = new RefBody();
    }

    public RefBody getRefBody() {
        return refBody;
    }

    public void registerInsnReference(Instruction instruction, Cell<Statement> statementCell) {
        List<Cell<Statement>> cells = this.instructionReferences.computeIfAbsent(instruction, x -> new ArrayList<>());
        cells.add(statementCell);
    }

    public Map<Instruction, List<Cell<Statement>>> getInstructionReferences() {
        return instructionReferences;
    }

    public RefLocal getLocal(StackLocal local) {
        return this.localMap.get(local);
    }

    public void convert() {
        convertLocals();
    }

    private void convertLocals() {
        for (StackLocal stackLocal : stackBody.getLocals()) {
            RefLocal refLocal = new RefLocal(null);
            refBody.getLocals().add(refLocal);
            localMap.put(stackLocal, refLocal);
        }

        refBody.setThisLocal(stackBody.getThisLocal().map(this::getLocal));
        refBody.setArgumentLocals(stackBody.getParameterLocals().stream()
                .map(this::getLocal)
                .collect(Collectors.toList()));
    }
}
