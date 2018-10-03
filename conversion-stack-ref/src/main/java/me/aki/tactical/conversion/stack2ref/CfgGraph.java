package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.TryCatchBlock;
import me.aki.tactical.ref.stmt.BranchStmt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CfgGraph {
    /**
     * Build a {@link CfgGraph} from a {@link RefBody}.
     *
     * @param body whose cfg should be build
     * @return the {@link CfgGraph} for a method
     */
    public static CfgGraph from(RefBody body) {
        Builder builder = new Builder(body);
        builder.build();
        return builder.graph;
    }

    private Node entryNode;
    private Set<Node> handlerNodes = new HashSet<>();
    private Set<Node> nodes = new HashSet<>();
    private Map<Statement, Node> nodeByStatement = new HashMap<>();

    private CfgGraph() {}

    /**
     * Get the {@link Node} that starts with the first statement of the method.
     *
     * @return entry node of the cfg.
     */
    public Node getEntryNode() {
        return entryNode;
    }

    /**
     * Get all nodes that start with an exception handler statement.
     *
     * @return nodes of all exception handlers.
     */
    public Set<Node> getHandlerNodes() {
        return handlerNodes;
    }

    /**
     * Get a {@link Set} of all cfg nodes of this graph.
     *
     * @return all cfg nodes
     */
    public Set<Node> getNodes() {
        return nodes;
    }

    /**
     * Get the node that contains a certain statement.
     *
     * If the statement is dead code, <tt>null</tt> gets returned.
     *
     * @param statement whose cfg is supported
     * @return node containing the statement or <tt>null</tt>.
     */
    public Node getNode(Statement statement) {
        return nodeByStatement.get(statement);
    }

    public static class Node {
        /**
         * All statements that of this node.
         */
        private List<Statement> statements = new ArrayList<>();

        /**
         * Nodes that might branch to this node.
         */
        private Set<Node> preceedingNodes = new HashSet<>();

        /**
         * Nodes that might be branched to after this node.
         */
        private Set<Node> succeedingNodes = new HashSet<>();

        public List<Statement> getStatements() {
            return statements;
        }

        public Set<Node> getPreceedingNodes() {
            return preceedingNodes;
        }

        public Set<Node> getSucceedingNodes() {
            return succeedingNodes;
        }
    }

    private static class Builder {
        private final RefBody body;
        private CfgGraph graph;

        private Map<Statement, Node> nodeByStatement = new HashMap<>();

        private Deque<WorkTask> worklist = new ArrayDeque<>();

        public Builder(RefBody body) {
            this.body = body;
            this.graph = new CfgGraph();
        }

        public void build() {
            enqueueWork(graph.entryNode = new Node(), body.getStatements().get(0));

            for (TryCatchBlock block : body.getTryCatchBlocks()) {
                Node node = new Node();
                graph.handlerNodes.add(node);
                enqueueWork(node, block.getHandler());
            }

            startWorking();

            graph.nodes.addAll(nodeByStatement.values());
            graph.nodeByStatement.putAll(this.nodeByStatement);
        }

        private void startWorking() {
            while (!worklist.isEmpty()) {
                WorkTask task = worklist.poll();
                Node currentNode = task.node;
                Iterator<Statement> iterator = body.getStatements().iterator(task.statement);

                Statement statement;
                do {
                    if (!iterator.hasNext()) {
                        throw new IllegalStateException("Corrupt method");
                    }
                    statement = iterator.next();

                    Node nextNode = nodeByStatement.get(statement);
                    if (nextNode != null && nextNode != currentNode) {
                        // we're visiting the first instruction of the next block.
                        nextNode.preceedingNodes.add(currentNode);
                        currentNode.succeedingNodes.add(nextNode);
                        break;
                    }

                    currentNode.statements.add(statement);
                    nodeByStatement.put(statement, currentNode);

                    if (statement instanceof BranchStmt) {
                        enqueueBranchTargets(currentNode, (BranchStmt) statement);
                    }
                } while (statement.continuesExecution());
            }
        }

        /**
         * Enqueue all not yet converted branch targets of a {@link BranchStmt} for conversion.
         *
         * @param node that contains the {@link BranchStmt}
         * @param statement {@link BranchStmt} whose branch targets are enqueued
         */
        private void enqueueBranchTargets(Node node, BranchStmt statement) {
            for (Statement targetStmt : statement.getBranchTargets()) {
                Node targetNode = getAndSplitNode(targetStmt);

                if (targetNode == null) {
                    targetNode = new Node();
                    enqueueWork(targetNode, targetStmt);
                }

                node.succeedingNodes.add(targetNode);
                targetNode.preceedingNodes.add(node);
            }
        }

        private void enqueueWork(Node node, Statement statement) {
            nodeByStatement.put(statement, node);
            worklist.add(new WorkTask(node, statement));
        }

        /**
         * Get a Node that starts with a certain instruction.
         *
         * If this instructions is in the middle of another node,
         * that node will get split into two nodes.
         *
         * If the statement is not a member of any node, <tt>null</tt> is returned.
         *
         * @param startStmt expected head of the requested Node
         * @return node starting with the statement or <tt>null</tt>
         */
        private Node getAndSplitNode(Statement startStmt) {
            Node node = nodeByStatement.get(startStmt);

            if (node == null) {
                return null;
            } else if (node.getStatements().get(0) == startStmt) {
                return node;
            } else {
                Node newHead = new Node();
                newHead.succeedingNodes.add(node);
                node.preceedingNodes.add(newHead);

                Iterator<Statement> iterator = node.getStatements().iterator();
                while (true) {
                    Statement statement = iterator.next();
                    if (statement == startStmt) {
                        return node;
                    }

                    newHead.statements.add(statement);
                    nodeByStatement.put(statement, newHead);
                    iterator.remove();
                }
            }
        }

        class WorkTask {
            /**
             * Node that should be worked on.
             *
             * It is empty and will be written to while this task gets processed.
             */
            private Node node;

            /**
             * First statement from which the conversion starts.
             */
            private Statement statement;

            public WorkTask(Node node, Statement statement) {
                this.node = node;
                this.statement = statement;
            }
        }
    }
}
