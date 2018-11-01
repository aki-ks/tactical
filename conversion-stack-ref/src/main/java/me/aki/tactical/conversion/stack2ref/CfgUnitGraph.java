package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.TryCatchBlock;
import me.aki.tactical.ref.stmt.BranchStmt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Build a Control-Flow-Graph of a method.
 * Each node in this graph corresponds to one statement.
 */
public class CfgUnitGraph {
    private final RefBody body;
    private final Map<Statement, Node> nodes = new IdentityHashMap<>();

    public CfgUnitGraph(RefBody body) {
        this.body = body;
        this.analyze();
    }

    private void analyze() {
        Set<Statement> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        InsertList<Statement> statements = body.getStatements();

        Deque<Node> worklist = new ArrayDeque<>();
        worklist.add(getOrCreateNode(statements.getFirst()));
        for (TryCatchBlock tryCatchBlock : body.getTryCatchBlocks()) {
            worklist.add(getOrCreateNode(tryCatchBlock.getHandler()));
        }

        while (!worklist.isEmpty()) {
            Node node = worklist.poll();
            Statement stmt = node.getStatement();

            if (visited.contains(stmt)) {
                continue;
            }

            getFollowingStatements(stmt).forEach(nextStmt -> {
                Node nextNode = getOrCreateNode(nextStmt);
                worklist.add(nextNode);

                node.succeeding.add(nextNode);
                nextNode.preceeding.add(node);
            });
        }
    }

    /**
     * Get all possible branch target of a statement.
     *
     * @param stmt get the branch locations of this statement
     * @return the branch targets of the statement
     */
    private Stream<Statement> getFollowingStatements(Statement stmt) {
        Stream<Statement> branches = stmt.continuesExecution() ?
                Stream.of(body.getStatements().getNext(stmt)) :
                Stream.empty();

        if (stmt instanceof BranchStmt) {
            List<Statement> branchTargets = ((BranchStmt) stmt).getBranchTargets();
            branches = Stream.concat(branches, branchTargets.stream());
        }

        return branches;
    }

    private Node getOrCreateNode(Statement statement) {
        Node node = nodes.get(statement);
        if (node == null) {
            nodes.put(statement, node = new Node(statement));
        }
        return node;
    }

    /**
     * Get the node corresponding to a statement.
     *
     * @param statement statement whose node is requested
     * @return node or <tt>null</tt> for dead code.
     */
    public Node getNode(Statement statement) {
        return nodes.get(statement);
    }

    /**
     * Store which statements might branch to a statement and where the statement might branch to.
     */
    public static class Node {
        /**
         * Statement of this node
         */
        private final Statement statement;

        /**
         * Get the nodes that branch to this node.
         */
        private final List<Node> preceeding = new ArrayList<>();

        /**
         * Get the nodes where this node might branch to.
         */
        private final List<Node> succeeding = new ArrayList<>();

        public Node(Statement statement) {
            this.statement = statement;
        }

        public Statement getStatement() {
            return statement;
        }

        public List<Node> getPreceeding() {
            return preceeding;
        }

        public List<Node> getSucceeding() {
            return succeeding;
        }
    }
}
