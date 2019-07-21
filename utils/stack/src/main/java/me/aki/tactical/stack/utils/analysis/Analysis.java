package me.aki.tactical.stack.utils.analysis;

import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.TryCatchBlock;
import me.aki.tactical.stack.insn.BranchInsn;
import me.aki.tactical.stack.insn.Instruction;
import me.aki.tactical.stack.utils.StackInsnReader;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes types on the stack and thereby finds dead code.
 */
public class Analysis {
    /**
     * Body of a method that should get analysed.
     */
    private final StackBody body;

    /**
     * Map instructions to the state of the stack before that instruction is executed.
     */
    private final Map<Instruction, Stack.Immutable<JvmType>> stackMap = new HashMap<>();

    /**
     * Was the analysis already started
     */
    private boolean didAnalyse = false;

    public Analysis(StackBody body) {
        this.body = body;
    }

    /**
     * Get the state of the stack before this instruction is executed.
     *
     * @param instruction whose stack state is requested
     * @return state of the stack or empty for unreachable instructions.
     */
    public Optional<Stack.Immutable<JvmType>> getStackState(Instruction instruction) {
        requireAnalysis();
        return Optional.ofNullable(stackMap.get(instruction));
    }

    private void requireAnalysis() {
        if (!this.didAnalyse) {
            throw new IllegalStateException("The analysis was not already run");
        }
    }

    public void analyze() {
        if (this.didAnalyse) {
            throw new IllegalStateException("The analysis was already run");
        }
        didAnalyse = true;

        startAnalysingFrom(new Workable(body.getInstructions().get(0), new Stack.Immutable<>()));

        Set<TryCatchBlock> notYetConvertedBlocks = new HashSet<>(body.getTryCatchBlocks());
        List<TryCatchBlock> blocksToConvert;
        do {
            blocksToConvert = notYetConvertedBlocks.stream()
                .filter(this::capturesReachableCode)
                .collect(Collectors.toList());

            for (TryCatchBlock block : blocksToConvert) {
                Stack.Mutable<JvmType> stack = new Stack.Mutable<>();
                stack.push(JvmType.REFERENCE); // put the caught exception on the stack
                startAnalysingFrom(new Workable(block.getHandler(), stack.toImmutable()));
            }

            notYetConvertedBlocks.removeAll(blocksToConvert);
        } while (!blocksToConvert.isEmpty());
    }

    /**
     * Check whether a TryCatchBlock encapsulates any reachable instruction.
     *
     * @param block try/catch block to check
     * @return does the try/catch block protect any reachable instructions.
     */
    private boolean capturesReachableCode(TryCatchBlock block) {
        Iterator<Instruction> iter = body.getInstructions().iterator(block.getFirst());
        while (iter.hasNext()) {
            Instruction instruction = iter.next();

            if (stackMap.containsKey(instruction)) {
                // "instruction" is reachable
                return true;
            }

            if (instruction == block.getLast()) {
                return false;
            }
        }

        // the block.getLast() instruction is either not in the instruction list of the method or
        // precedes the block.getFirst() instruction.
        throw new IllegalStateException();
    }

    /**
     * Emulate the execution of the method and thereby compute the types on the stack.
     *
     * @param workable start point of execution
     */
    private void startAnalysingFrom(Workable workable) {
        Deque<Workable> worklist = new ArrayDeque<>(List.of(workable));

        StackEmulatingInsnVisitor<Instruction, StackLocal> stackEmulator = new StackEmulatingInsnVisitor<>(null, new Stack.Mutable<>());
        StackInsnReader insnReader = new StackInsnReader(stackEmulator);

        while (!worklist.isEmpty()) {
            Workable work = worklist.poll();
            stackEmulator.getStack().loadFrom(work.stackState);

            Iterator<Instruction> iterator = body.getInstructions().iterator(work.firstInsn);
            Instruction instruction;
            do {
                if (!iterator.hasNext()) {
                    throw new IllegalStateException("Illegal end of method");
                }

                instruction = iterator.next();

                Stack.Immutable<JvmType> currentFrame = stackEmulator.getStack().toImmutable();
                Stack.Immutable<JvmType> expectedFrame = stackMap.get(instruction);
                if (expectedFrame != null) {
                    // The code starting from here on has already been visited.
                    // The stack must have the same state as during the last visit.
                    if (expectedFrame.isEqual(currentFrame)) {
                        break;
                    } else {
                        throw new IllegalStateException("Different stack states");
                    }
                }

                // store the stack state before the instruction was evaluated
                stackMap.put(instruction, currentFrame);

                // evaluate the stack changes done by this instruction
                insnReader.accept(instruction);

                if (instruction instanceof BranchInsn) {
                    currentFrame = stackEmulator.getStack().toImmutable();
                    for (Instruction branchTarget : ((BranchInsn) instruction).getBranchTargets()) {
                        worklist.add(new Workable(branchTarget, currentFrame));
                    }
                }
            } while (instruction.continuesExecution());
        }
    }

    class Workable {
        private Instruction firstInsn;
        private Stack.Immutable<JvmType> stackState;

        public Workable(Instruction firstInsn, Stack.Immutable<JvmType> stackState) {
            this.stackState = stackState;
            this.firstInsn = firstInsn;
        }
    }
}
