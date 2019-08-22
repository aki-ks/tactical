package me.aki.tactical.core.utils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Build a Control-Flow-Graph of a method.
 * Each node in this graph corresponds to one instruction.
 *
 * @param <I> type of instructions in the cfg graph
 */
public abstract class AbstractCfgGraph<I> {
    /**
     * Map instructions to their corresponding node.
     * This map contains only instructions that can possibly be reached.
     */
    protected final Map<I, Node> nodes = new HashMap<>();

    private Collection<TryCatchBlock> tryCatchBlocks;

    /**
     * Start building the CFG graph.
     */
    protected void analyze() {
        this.tryCatchBlocks = getTryCatchBlocks();

        startAnalyzingFrom(getHeadInsn());

        List<TryCatchBlock> reachableTryCatchBlocks = tryCatchBlocks.stream()
                .filter(block -> !isDeadCode(block.start, block.end))
                .collect(Collectors.toList());

        for (TryCatchBlock tryCatchBlock : reachableTryCatchBlocks) {
            startAnalyzingFrom(tryCatchBlock.handler);
        }
    }

    private void startAnalyzingFrom(I start) {
        Deque<Node> worklist = new ArrayDeque<>();
        worklist.add(getOrCreateNode(start));

        while (!worklist.isEmpty()) {
            Node node = worklist.poll();
            if (node.visited) {
                // Node already visited
                continue;
            }
            node.visited = true;

            getReachableInstructions(node.getInstruction()).forEach(nextStmt -> {
                if (nextStmt == null) {
                    throw new IllegalStateException("Illegal end of method");
                } else {
                    Node nextNode = getOrCreateNode(nextStmt);
                    worklist.add(nextNode);

                    node.succeeding.add(nextNode);
                    nextNode.preceding.add(node);
                }
            });
        }
    }

    protected Node getOrCreateNode(I insn) {
        Node node = nodes.get(insn);
        if (node == null) {
            nodes.put(insn, node = new Node(insn));
        }
        return node;
    }

    /**
     * Get the node of the first instruction of the method.
     *
     * @return entry point of the method
     */
    public Node getHead() {
        return getNode(getHeadInsn());
    }

    /**
     * Get the nodes for all instructions that are exception handlers of a try/catch blocks.
     *
     * @return all try/catch handler nodes
     */
    public Set<Node> getHandlerNodes() {
        return tryCatchBlocks.stream()
                .map(block -> block.handler)
                .map(this::getNode)
                .collect(Collectors.toSet());
    }

    /**
     * Get all Nodes of this cfg graph.
     *
     * @return all nodes of the cfg
     */
    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /**
     * Get the node corresponding to an instruction.
     *
     * @param instruction instruction whose node is requested
     * @return node or <tt>null</tt> for dead code.
     */
    public Node getNode(I instruction) {
        return nodes.get(instruction);
    }

    /**
     * Check whether a instruction cannot be reached.
     *
     * @param instruction the instruction
     * @return is the instruction dead code.
     */
    public boolean isDeadCode(I instruction) {
        return !nodes.containsKey(instruction);
    }



    /**
     * Get the first instruction of the method that is its entry point.
     *
     * @return the entrypoint to the method
     */
    protected abstract I getHeadInsn();

    /**
     * Get a list of all try/catch blocks.
     *
     * @return all available try/catch blocks
     */
    protected abstract Collection<TryCatchBlock> getTryCatchBlocks();

    /**
     * Get all instructions that may be reached after an instruction.
     * Either since they are succeeding the instruction or
     * the instruction may branch to those instructions.
     *
     * @param instruction which instructions may be reached from this instruction
     * @return all instructions that may be reached from the instruction
     */
    protected abstract Stream<I> getReachableInstructions(I instruction);

    /**
     * Check whether a range of instructions contains only dead code.
     *
     * @param start first instruction of the range
     * @param end last instruction of the range
     * @return does the range only contain deadcode
     */
    public abstract boolean isDeadCode(I start, I end);

    /**
     * Execute a function for each node in the CFG graph.
     *
     * @param function the function to be executed
     */
    public void forEachNode(Consumer<Node> function) {
        Set<Node> visited = new HashSet<>();
        Deque<Node> worklist = new ArrayDeque<>();

        worklist.add(getHead());
        worklist.addAll(getHandlerNodes());

        while (!worklist.isEmpty()) {
            Node node = worklist.poll();
            if (!visited.add(node)) {
                continue;
            }

            function.accept(node);

            worklist.addAll(node.getSucceeding());
        }
    }


    protected class TryCatchBlock {
        /**
         * The first instruction of the range within this block catches exceptions.
         */
        private final I start;

        /**
         * The last (inclusive) instruction of the range within this block catches exceptions.
         */
        private final I end;

        /**
         * The instruction that handles caught exceptions
         */
        private final I handler;

        public TryCatchBlock(I start, I end, I handler) {
            this.start = start;
            this.end = end;
            this.handler = handler;
        }
    }

    /**
     * A Node in the Cfg graph that corresponds to one instruction that can be reached.
     * It knows which other nodes may reach it and which nodes it can reach.
     */
    public class Node {
        /**
         * instruction corresponding to this node
         */
        private final I instruction;

        /**
         * Get the nodes that branch to this node.
         */
        private final Set<Node> preceding = new HashSet<>();

        /**
         * Get the nodes where this node might branch to.
         */
        private final Set<Node> succeeding = new HashSet<>();

        private boolean visited = false;

        public Node(I instruction) {
            this.instruction = instruction;
        }

        public I getInstruction() {
            return instruction;
        }

        public Set<Node> getPreceding() {
            return preceding;
        }

        public Set<Node> getSucceeding() {
            return succeeding;
        }
    }
}
