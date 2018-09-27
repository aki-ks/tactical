package me.aki.tactical.conversion.stack2asm.analysis;

import me.aki.tactical.conversion.stack2asm.TacticalInsnReader;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.TryCatchBlock;
import me.aki.tactical.stack.insn.BranchInsn;
import me.aki.tactical.stack.insn.Instruction;

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
    private final Map<Instruction, Stack.Immutable> stackMap = new HashMap<>();

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
    public Optional<Stack.Immutable> getStackState(Instruction instruction) {
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

        startAnalysingFrom(new Workable(body.getInstructions().get(0), new Stack.Immutable()));

        Set<TryCatchBlock> notYetConvertedBlocks = new HashSet<>(body.getTryCatchBlocks());
        List<TryCatchBlock> blocksToConvert;
        do {
            blocksToConvert = notYetConvertedBlocks.stream()
                .filter(this::capturesReachableCode)
                .collect(Collectors.toList());

            for (TryCatchBlock block : blocksToConvert) {
                Stack.Mutable stack = new Stack.Mutable();
                stack.push(JvmType.REFERENCE); // put the caught exception on the stack
                startAnalysingFrom(new Workable(block.getHandler(), stack.immutableCopy()));
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
        int startIndex = body.getInstructions().indexOf(block.getFirst());
        Iterator<Instruction> iter = body.getInstructions().listIterator(startIndex);
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
        List<Instruction> instructions = body.getInstructions();

        Deque<Workable> worklist = new ArrayDeque<>(List.of(workable));

        StackEmulatingInsnVisitor<Instruction> stackEmulator = new StackEmulatingInsnVisitor<>(null, new Stack.Mutable());
        TacticalInsnReader insnReader = new TacticalInsnReader(stackEmulator);

        while (!worklist.isEmpty()) {
            Workable work = worklist.poll();
            stackEmulator.getStack().loadFrom(work.stackState);

            int startIndex = instructions.indexOf(work.firstInsn);
            if (startIndex < 0) {
                throw new IllegalStateException();
            }

            Iterator<Instruction> iter = instructions.listIterator(startIndex);
            Instruction instruction;
            do {
                if (!iter.hasNext()) {
                    throw new IllegalStateException("Illegal end of method");
                }

                instruction = iter.next();

                if (stackMap.containsKey(instruction)) {
                    // The code starting from this instruction has already been visited
                    break;
                }

                // store the stack state before the instruction was evaluated
                stackMap.put(instruction, stackEmulator.getStack().immutableCopy());

                // evaluate the stack changes done by this instruction
                insnReader.accept(instruction);

                if (instruction instanceof BranchInsn) {
                    for (Instruction branchTarget : ((BranchInsn) instruction).getBranchTargets()) {
                        worklist.add(new Workable(branchTarget, stackEmulator.getStack().immutableCopy()));
                    }
                }
            } while (instruction.continuesExecution());
        }
    }

    class Workable {
        private Instruction firstInsn;
        private Stack.Immutable stackState;

        public Workable(Instruction firstInsn, Stack.Immutable stackState) {
            this.stackState = stackState;
            this.firstInsn = firstInsn;
        }
    }
}
