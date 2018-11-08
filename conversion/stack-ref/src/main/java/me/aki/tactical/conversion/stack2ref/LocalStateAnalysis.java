package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.stmt.AssignStmt;
import me.aki.tactical.ref.util.CommonOperations;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Analyse the possible values that locals may have at certain locations in code.
 */
public class LocalStateAnalysis {
    private final RefBody body;
    private final CfgUnitGraph graph;
    private final Map<RefLocal, LocalStates> locals;
    private final Map<RefLocal, List<Statement>> localReadMap;
    private final Map<RefLocal, List<AssignStmt>> localWriteMap;

    public LocalStateAnalysis(CfgUnitGraph graph) {
        this(graph, CommonOperations.getLocalReadMap(graph.getBody()), CommonOperations.getLocalWriteMap(graph.getBody()));
    }

    public LocalStateAnalysis(CfgUnitGraph graph, Map<RefLocal, List<Statement>> localReadMap, Map<RefLocal, List<AssignStmt>> localWriteMap) {
        this.graph  = graph;
        this.body = graph.getBody();

        this.localReadMap = localReadMap;
        this.localWriteMap = localWriteMap;

        this.locals = body.getLocals().stream().collect(Collectors.toMap(Function.identity(), LocalStates::new));
    }


    /**
     * Get a analysis of the possible states of a locals.
     *
     * @param local
     * @return
     */
    public LocalStates getLocalStates(RefLocal local) {
        return locals.get(local);
    }

    public class LocalStates {
        private final RefLocal local;
        private final Set<Statement> reads;
        private final Set<AssignStmt> writes;

        /**
         * Map statements that reference the local to all possible states of the local.
         */
        private Map<Statement, Set<State>> stmtToStates;

        private Map<State, Set<Statement>> stateToStmts;

        LocalStates(RefLocal local) {
            this.local = local;
            this.reads = new HashSet<>(localReadMap.getOrDefault(local, Collections.emptyList()));
            this.writes = new HashSet<>(localWriteMap.getOrDefault(local, Collections.emptyList()));

            buildStateMap();
        }

        /**
         * Get a set of all statements that read from the local.
         *
         * @return statements that access the local
         */
        public Set<Statement> getReads() {
            return reads;
        }

        /**
         * Get all assign statements that store a value in the local.
         *
         * @return all statements that write to the local.
         */
        public Set<AssignStmt> getWrites() {
            return writes;
        }

        /**
         * Get the possible states of the local at a certain location in code.
         *
         * @param statement the location in code.
         * @return all possible states of the local
         */
        public Set<State> getStates(Statement statement) {
            return stmtToStates.getOrDefault(statement, Collections.emptySet());
        }

        /**
         * Get all statements where the local may have a certain state.
         *
         * @param state the requested state
         * @return all statements that may have the requested state
         */
        public Set<Statement> getStatement(State state) {
            return stateToStmts.getOrDefault(state, Collections.emptySet());
        }

        private void buildStateMap() {
            this.stmtToStates = new HashMap<>();
            this.stateToStmts = new HashMap<>();

            for (State assign : getLocalStates(body, local, graph, writes)) {
                analyzeCfgForward(assign.getNode(), node -> {
                    Statement stmt = node.getStatement();
                    this.stmtToStates.computeIfAbsent(stmt, x -> new HashSet<>()).add(assign);
                    this.stateToStmts.computeIfAbsent(assign, x -> new HashSet<>()).add(stmt);

                    return !writes.contains(stmt);
                });
            }
        }

        private List<State> getLocalStates(RefBody body, RefLocal local, CfgUnitGraph graph, Set<AssignStmt> writes) {
            final List<State> assign = new ArrayList<>();

            body.getThisLocal().ifPresent(thisLocal -> {
                if (thisLocal == local) {
                    assign.add(new State.This(graph.getHead()));
                }
            });

            int paramIndex = 0;
            for (RefLocal argumentLocal : body.getArgumentLocals()) {
                if (argumentLocal == local) {
                    assign.add(new State.Parameter(graph.getHead(), paramIndex));
                }
                paramIndex += 1;
            }

            for (AssignStmt write : writes) {
                assign.add(new State.Stmt(graph.getNode(body.getStatements().getNext(write)), write));
            }

            return assign;
        }

        /**
         * Walk forward through the CFG starting at a certain node.
         * The function passed as parameter is evaluated for every node on the way.
         * If the lambda evaluates to <tt>false</tt>, then the succeeding nodes will not be visited.
         *
         * Nodes that have already been visited will not be visited again.
         *
         * @param start start the analysis from that node
         * @param visit the function that is evaluated for each visited node
         */
        private void analyzeCfgForward(CfgUnitGraph.Node start, Function<CfgUnitGraph.Node, Boolean> visit) {
            Deque<CfgUnitGraph.Node> worklist = new ArrayDeque<>();
            Set<CfgUnitGraph.Node> visited = new HashSet<>();
            worklist.add(start);

            while (!worklist.isEmpty()) {
                CfgUnitGraph.Node node = worklist.poll();

                boolean alreadyVisited = !visited.add(node);
                if (alreadyVisited) {
                    continue;
                }

                if (visit.apply(node)) {
                    worklist.addAll(node.getSucceeding());
                }
            }
        }
    }

    /**
     * A possible state of a local.
     */
    public static interface State {
        /**
         * Get the Node from whereon this state is assigned to the local.
         *
         * @return node where the assignment of the state occurs
         */
        CfgUnitGraph.Node getNode();

        /**
         * Represents that a local has the this value assigned.
         * Therefore it must be the {@link RefBody#getThisLocal() this local}.
         */
        public static class This implements State {
            private final CfgUnitGraph.Node headNode;

            public This(CfgUnitGraph.Node headNode) {
                this.headNode = headNode;
            }

            @Override
            public CfgUnitGraph.Node getNode() {
                return headNode;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                This aThis = (This) o;
                return Objects.equals(headNode, aThis.headNode);
            }

            @Override
            public int hashCode() {
                return Objects.hash(headNode);
            }

            @Override
            public String toString() {
                return This.class.getSimpleName() + '{' +
                        "headNode=" + headNode +
                        '}';
            }
        }

        /**
         * Represents that a local has a parameter value assigned.
         * Therefore it must be a {@link RefBody#getArgumentLocals() parameter local}.
         */
        public static class Parameter implements State {
            private final CfgUnitGraph.Node headNode;
            private final int index;

            public Parameter(CfgUnitGraph.Node headNode, int index) {
                this.headNode = headNode;
                this.index = index;
            }

            @Override
            public CfgUnitGraph.Node getNode() {
                return headNode;
            }

            public int getIndex() {
                return index;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Parameter parameter = (Parameter) o;
                return index == parameter.index &&
                        Objects.equals(headNode, parameter.headNode);
            }

            @Override
            public int hashCode() {
                return Objects.hash(headNode, index);
            }

            @Override
            public String toString() {
                return Parameter.class.getSimpleName() + '{' +
                        "headNode=" + headNode +
                        ", index=" + index +
                        '}';
            }
        }

        /**
         * Represents that a value got assigned to a local by a {@link AssignStmt}
         */
        public static class Stmt implements State {
            /**
             * The node of the instruction <tt>succeeding</tt> the assign statement.
             * From there the local has the just assigned value.
             */
            private final CfgUnitGraph.Node node;

            /**
             * The assign statement that writes to the local.
             */
            private final AssignStmt assign;

            public Stmt(CfgUnitGraph.Node node, AssignStmt assign) {
                this.node = node;
                this.assign = assign;
            }

            @Override
            public CfgUnitGraph.Node getNode() {
                return node;
            }

            public AssignStmt getStatement() {
                return assign;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Stmt stmt = (Stmt) o;
                return Objects.equals(node, stmt.node) &&
                        Objects.equals(assign, stmt.assign);
            }

            @Override
            public int hashCode() {
                return Objects.hash(node, assign);
            }

            @Override
            public String toString() {
                return Stmt.class.getSimpleName() + '{' +
                        "node=" + node +
                        ", assign=" + assign +
                        '}';
            }
        }
    }
}
