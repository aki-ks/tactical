package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.conversion.stackasm.StackInsnReader;
import me.aki.tactical.conversion.stackasm.analysis.Analysis;
import me.aki.tactical.conversion.stackasm.analysis.Stack;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.LocalVariableTypeAnnotation;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.TryCatchBlock;
import me.aki.tactical.ref.stmt.AssignStatement;
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
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility that builds a {@link RefBody} from a {@link StackBody}.
 */
public class BodyConverter {
    private final StackBody stackBody;
    private final RefBody refBody;
    private Analysis analysis;

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
        this.analysis = new Analysis(this.stackBody);
        this.analysis.analyze();

        convertLocals();

        convertInsns();

        resolveInsnsRefs();

        convertLocalVariables();
        convertLocalVariableAnnotations();
        convertLineNumbers();
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

        // Convert all try/catch blocks and add their handlers to the worklist.
        this.stackBody.getTryCatchBlocks().forEach(stackTryCatchBlock -> {
            if (isRangeEmpty(stackTryCatchBlock.getFirst(), stackTryCatchBlock.getLast())) {
                return;
            }

            Optional<Path> exception = stackTryCatchBlock.getExceptionType();
            ObjectType exceptionType = new ObjectType(exception.orElse(Path.THROWABLE));
            RefLocal caughtExceptionLocal = newLocal(exceptionType);

            Stack.Mutable<StackValue> stack = new Stack.Mutable<>();
            stack.push(new StackValue(stackTryCatchBlock.getHandler(), caughtExceptionLocal));
            worklist.add(new CfgNode(stackTryCatchBlock.getHandler(), stack.immutableCopy()));

            convertTryCatchBlock(stackTryCatchBlock, caughtExceptionLocal);
        });

        while (!worklist.isEmpty()) {
            CfgNode node = worklist.poll();
            this.stack.loadFrom(node.stack);

            int startIndex = instructions.indexOf(node.instruction);
            Iterator<Instruction> insnIter = instructions.listIterator(startIndex);
            Instruction instruction;
            do {
                if (insnIter.hasNext()) {
                    instruction = insnIter.next();
                } else {
                    throw new IllegalStateException("Unexpected end of method");
                }

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

    /**
     * Convert a stack try/catch block to a ref try/catch block.
     *
     * @param stackTryCatchBlock try/catch block to be converted
     * @param caughtExceptionLocal local that contains exceptions caught by the try/catch block
     */
    private void convertTryCatchBlock(me.aki.tactical.stack.TryCatchBlock stackTryCatchBlock, RefLocal caughtExceptionLocal) {
        Optional<Path> exception = stackTryCatchBlock.getExceptionType();
        TryCatchBlock refTryCatchBlock = new TryCatchBlock(null, null, null, exception, caughtExceptionLocal);
        refTryCatchBlock.getTypeAnnotations().addAll(stackTryCatchBlock.getTypeAnnotations());

        registerInsnReference(stackTryCatchBlock.getFirst(), refTryCatchBlock.getFirstCell());
        registerInsnReference(stackTryCatchBlock.getLast(), refTryCatchBlock.getLastCell());
        registerInsnReference(stackTryCatchBlock.getHandler(), refTryCatchBlock.getHandlerCell());

        this.refBody.getTryCatchBlocks().add(refTryCatchBlock);
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
    private class VisitRecord {
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

    /**
     * Check whether a range of instructions consists only out of dead code.
     *
     * @param start first instruction of the instruction range
     * @param end last instruction of the instruction range
     * @return contains the instruction range only dead code
     */
    private boolean isRangeEmpty(Instruction start, Instruction end) {
        List<Instruction> instructions = this.stackBody.getInstructions();
        int startIndex = instructions.indexOf(start);
        Iterator<Instruction> insnIter = instructions.listIterator(startIndex);

        while (insnIter.hasNext()) {
            Instruction instruction = insnIter.next();

            if (!isDeadCode(instruction)) {
                // This instruction is no dead code
                return false;
            }

            if (instruction == end) {
                return true;
            }
        }

        throw new RuntimeException("Illegal instruction range");
    }

    /**
     * Check whether an instruction can be reached.
     *
     * @param instruction that gets checked
     * @return is the instruction non-reachable
     */
    private boolean isDeadCode(Instruction instruction) {
        return !analysis.getStackState(instruction).isPresent();
    }

    private void resolveInsnsRefs() {
        this.instructionReferences.forEach((insn, stmtCells) -> {
            Statement stmt = getCorrespondingStmt(insn);
            stmtCells.forEach(cell -> cell.set(stmt));
        });
    }

    /**
     * Find the converted {@link Statement} that corresponds to an {@link Instruction}.
     *
     * If the instruction converted to an expression, that expression will be stored in a local
     * and the {@link AssignStatement} is returned.
     *
     * If the instruction is neither a statement nor an expression, than the statement
     * corresponding to the next instruction is returned.
     *
     * @param instruction whose correspondent statement we want.
     * @return statement that corresponding to the instruction
     */
    private Statement getCorrespondingStmt(Instruction instruction) {
        Optional<Statement> correspondingLocalOpt = getCorrespondingStmtOpt(instruction);
        if (correspondingLocalOpt.isPresent()) {
            return correspondingLocalOpt.get();
        }

        List<Instruction> instructions = this.stackBody.getInstructions();
        int startIndex = instructions.indexOf(instruction);
        Iterator<Instruction> insnIter = instructions.listIterator(startIndex + 1);
        while (insnIter.hasNext()) {
            correspondingLocalOpt = getCorrespondingStmtOpt(insnIter.next());
            if (correspondingLocalOpt.isPresent()) {
                return correspondingLocalOpt.get();
            }
        }

        // Should not be reachable for valid bytecode
        throw new AssertionError();
    }

    private Optional<Statement> getCorrespondingStmtOpt(Instruction instruction) {
        List<Statement> convertedStatements = this.convertedStatements.get(instruction);
        if (convertedStatements != null && !convertedStatements.isEmpty()) {
            // The instruction did convert into a statement,
            return Optional.of(convertedStatements.get(0));
        } else {
            StackDelta delta = this.stackDeltaMap.get(instruction);

            if (delta != null && delta.getPush().isPresent()) {
                StackValue push = delta.getPush().get();
                return Optional.of(push.storeInLocal(this, newLocal()));
            } else {
                // This instruction converts neither to a statement, not does it push a value.
                // Therefore there cannot be a corresponding stmt.
                return Optional.empty();
            }
        }
    }

    private void convertLocalVariables() {
        for (StackBody.LocalVariable stackLocalVariable : stackBody.getLocalVariables()) {
            if (isRangeEmpty(stackLocalVariable.getStart(), stackLocalVariable.getEnd())) {
                continue;
            }

            String name = stackLocalVariable.getName();
            Type type = stackLocalVariable.getType();
            Optional<String> signature = stackLocalVariable.getSignature();
            Statement start = getCorrespondingStmt(stackLocalVariable.getStart());
            Statement end = getCorrespondingStmt(stackLocalVariable.getEnd());
            RefLocal local = getLocal(stackLocalVariable.getLocal());

            refBody.getLocalVariables().add(new RefBody.LocalVariable(name, type, signature, start, end, local));
        }
    }

    private void convertLocalVariableAnnotations() {
        for (StackBody.LocalVariableAnnotation localVarAnno : stackBody.getLocalVariableAnnotations()) {
            LocalVariableTypeAnnotation annotation = localVarAnno.getAnnotation();
            List<RefBody.LocalVariableAnnotation.Location> locations = localVarAnno.getLocations().stream()
                    .filter(location -> !isRangeEmpty(location.getStart(), location.getEnd()))
                    .map(location -> new RefBody.LocalVariableAnnotation.Location(
                            getCorrespondingStmt(location.getStart()),
                            getCorrespondingStmt(location.getEnd()),
                            getLocal(location.getLocal())))
                    .collect(Collectors.toList());

            if (!locations.isEmpty()) {
                refBody.getLocalVariableAnnotations().add(new RefBody.LocalVariableAnnotation(annotation, locations));
            }
        }
    }

    private void convertLineNumbers() {
        stackBody.getLineNumbers().stream()
                .filter(line -> !isDeadCode(line.getInstruction()))
                .map(line -> new RefBody.LineNumber(line.getLine(),
                        getCorrespondingStmt(line.getInstruction())))
                .forEach(refBody.getLineNumbers()::add);
    }
}
