package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.conversion.stackasm.StackInsnReader;
import me.aki.tactical.conversion.stackasm.analysis.Stack;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.insn.BranchInsn;
import me.aki.tactical.stack.insn.Instruction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
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

    /**
     * Map instructions to the statements that represent them.
     */
    private final Map<Instruction, List<Statement>> convertedStatements = new HashMap<>();

    private final Map<Instruction, StackDelta> stackDeltaMap = new HashMap<>();

    private final Stack.Mutable<StackValue> stack = new Stack.Mutable<>();

    public BodyConverter(StackBody stackBody) {
        this.stackBody = stackBody;
        this.refBody = new RefBody();
    }

    public RefBody getRefBody() {
        return refBody;
    }

    public RefLocal getLocal(StackLocal local) {
        return this.localMap.get(local);
    }

    /**
     * Create and add a new local without setting its type.
     *
     * @return a new local
     */
    public RefLocal newLocal() {
        return newLocal(null);
    }

    /**
     * Create a new local and add it to the body.
     *
     * @param type of the created local
     * @return a new local
     */
    public RefLocal newLocal(Type type) {
        RefLocal local = new RefLocal(type);
        this.refBody.getLocals().add(local);
        return local;
    }

    public void registerInsnReference(Instruction instruction, Cell<Statement> statementCell) {
        List<Cell<Statement>> cells = this.instructionReferences.computeIfAbsent(instruction, x -> new ArrayList<>());
        cells.add(statementCell);
    }

    public Map<Instruction, List<Statement>> getConvertedStatements() {
        return convertedStatements;
    }

    public void addStatement(Instruction instruction, Statement statement) {
        List<Statement> statements = convertedStatements.computeIfAbsent(instruction, x -> new ArrayList<>());
        statements.add(statement);
    }

    public Map<Instruction, StackDelta> getStackDeltaMap() {
        return stackDeltaMap;
    }

    public StackValue pop() {
        return this.stack.pop();
    }

    public void push(StackValue stackValue) {
        this.stack.push(stackValue);
    }

    public void convert() {
        convertLocals();

        convertInsns();
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

    private void convertInsns() {
        VisitRecord record = new VisitRecord();
        Queue<CfgNode> worklist = new ArrayDeque<>();

        // Add the first instruction to the work list
        List<Instruction> instructions = this.stackBody.getInstructions();
        Instruction firstInstruction = instructions.get(0);
        worklist.add(new CfgNode(firstInstruction, new Stack.Immutable<>()));

        this.stackBody.getTryCatchBlocks().forEach(tryCatchBlock -> {
            Stack.Mutable<StackValue> stack = new Stack.Mutable<>();
            ObjectType exceptionType = new ObjectType(tryCatchBlock.getExceptionType().orElse(Path.THROWABLE));
            RefLocal caughtExceptionLocal = newLocal(exceptionType);

            stack.push(new StackValue(tryCatchBlock.getHandler(), caughtExceptionLocal));
            worklist.add(new CfgNode(tryCatchBlock.getHandler(), stack.immutableCopy()));
        });

        while (!worklist.isEmpty()) {
            CfgNode node = worklist.poll();
            this.stack.loadFrom(node.stack);

            int startIndex = instructions.indexOf(node.instruction);
            Iterator<Instruction> insnIter = instructions.listIterator(startIndex);
            Instruction instruction;
            do {
                if (!insnIter.hasNext()) {
                    throw new IllegalStateException("Unexpected end of method");
                }

                instruction = insnIter.next();

                convertInstruction(instruction);

                if (instruction instanceof BranchInsn) {
                    BranchInsn branchInsn = (BranchInsn) instruction;
                    for (Instruction branchTarget : branchInsn.getBranchTargets()) {
                        if (!record.wasVisited(branchInsn, branchTarget)) {
                            worklist.add(new CfgNode(instruction, this.stack.immutableCopy()));
                            record.setVisited(branchInsn, branchTarget);
                        }
                    }
                }
            } while (instruction.continuesExecution());
        }

    }

    private class CfgNode {
        /**
         * First instruction of this cfg block.
         */
        private Instruction instruction;

        /**
         * Values on the stack
         */
        private Stack.Immutable<StackValue> stack;

        public CfgNode(Instruction instruction, Stack.Immutable<StackValue> stack) {
            this.instruction = instruction;
            this.stack = stack;
        }
    }

    /**
     * Store what branches have already been visited.
     */
    class VisitRecord {
        /**
         * Map an instruction to all instruction that branch to it and have already been visited.
         */
        private Map<Instruction, Set<BranchInsn>> visited = new HashMap<>();

        /**
         * Check whether a branch from one instruction to another one was already visited.
         *
         * @param cause instruction that causes the branch
         * @param target branch location
         * @return was this branch already visited.
         */
        public boolean wasVisited(BranchInsn cause, Instruction target) {
            Set<BranchInsn> visitedCauses = visited.get(target);
            return visitedCauses != null && visitedCauses.contains(cause);
        }

        /**
         * Mark a branch from one instruction to another one as visited.
         *
         * @param cause instruction that causes the branch
         * @param target branch location
         */
        public void setVisited(BranchInsn cause, Instruction target) {
            Set<BranchInsn> visitedCauses = visited.computeIfAbsent(target, x -> new HashSet<>());
            visitedCauses.add(cause);
        }
    }

    private void convertInstruction(Instruction instruction) {
        RefInsnWriter writer = new RefInsnWriter(this);
        StackInsnReader reader = new StackInsnReader(writer);

        writer.setInstruction(instruction);
        reader.accept(instruction);
    }
}
