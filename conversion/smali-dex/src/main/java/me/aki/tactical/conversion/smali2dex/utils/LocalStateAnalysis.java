package me.aki.tactical.conversion.smali2dex.utils;

import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.Instruction;
import me.aki.tactical.dex.utils.CommonOperations;
import me.aki.tactical.dex.utils.DexCfgGraph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Analyses which instructions may have written the values that is stored at a certain location in code in registers.
 */
public class LocalStateAnalysis {
    private final DexCfgGraph cfgGraph;
    private final DexBody body;

    /**
     * Store for each register which instructions read from it
     */
    private Map<Register, Set<Instruction>> readMap;

    /**
     * Store for each
     */
    private Map<Register, Set<Instruction>> writeMap;

    /**
     * Map registers to details about their possible values
     */
    private Map<Register, RegisterStates> registerStatesMap;

    public LocalStateAnalysis(DexCfgGraph cfgGraph) {
        this.cfgGraph = cfgGraph;
        this.body = cfgGraph.getBody();

        this.readMap = CommonOperations.getReadMap(cfgGraph.getBody());
        this.writeMap = CommonOperations.getWriteMap(cfgGraph.getBody());

        this.registerStatesMap = cfgGraph.getBody().getRegisters().stream()
                .collect(Collectors.toMap(Function.identity(), RegisterStates::new));
    }

    public Map<Register, Set<Instruction>> getReadMap() {
        return Collections.unmodifiableMap(readMap);
    }

    public Map<Register, Set<Instruction>> getWriteMap() {
        return Collections.unmodifiableMap(writeMap);
    }

    public RegisterStates getRegisterState(Register register) {
        return registerStatesMap.get(register);
    }

    public class RegisterStates {
        private final Register register;

        private final Set<Instruction> reads;
        private final Set<Instruction> writes;

        /**
         * Map instructions to the states that they may possibly have at that location in code.
         */
        private final Map<Instruction, Set<RegisterState>> insnToState = new HashMap<>();

        /**
         * Map states to all instructions where the register may have them.
         */
        private final Map<RegisterState, Set<Instruction>> stateToInsn = new HashMap<>();

        /**
         * A List of all assignment groups.
         */
        private final List<Set<RegisterState>> groups = new ArrayList<>();

        public RegisterStates(Register register) {
            this.register = register;

            this.reads = Collections.unmodifiableSet(readMap.getOrDefault(register, Set.of()));
            this.writes = Collections.unmodifiableSet(writeMap.getOrDefault(register, Set.of()));

            this.buildStateMap();
            this.buildAssignGroup();
        }

        /**
         * Get a set of all instructions that read from this register.
         *
         * @return all instructions reading from the register
         */
        public Set<Instruction> getReads() {
            return Collections.unmodifiableSet(reads);
        }

        /**
         * Get a set of all instructions that write to this register.
         *
         * @return all instructions writing to the register
         */
        public Set<Instruction> getWrites() {
            return Collections.unmodifiableSet(writes);
        }

        /**
         * Get all instructions where the register have a certain state
         *
         * @param state the requested state
         * @return instructions where the stats is possibly presen
         */
        public Set<Instruction> getInstructions(RegisterState state) {
            return Collections.unmodifiableSet(stateToInsn.getOrDefault(state, Set.of()));
        }

        /**
         * Get all states that the register may possibly have at a certain location in code.
         *
         * @param instruction get the states at this location in code
         * @return all possibly present states
         */
        public Set<RegisterState> getStates(Instruction instruction) {
            return Collections.unmodifiableSet(insnToState.getOrDefault(instruction, Set.of()));
        }

        /**
         * Get a groups of states that are independent of each other.
         *
         * @return groups of independent states
         */
        public List<Set<RegisterState>> getGroups() {
            return groups;
        }

        private void buildStateMap() {
            for (RegisterState assign : getAllStates()) {
                // Propagate the state forward until some other instruction writes onto the register.
                // From there on this state is no longer present
                walkCfgForward(assign.getNode(), node -> {
                    Instruction stmt = node.getInstruction();

                    this.insnToState.computeIfAbsent(stmt, x -> new HashSet<>()).add(assign);
                    this.stateToInsn.computeIfAbsent(assign, x -> new HashSet<>()).add(stmt);

                    return !writes.contains(stmt);
                });

            }
        }

        /**
         * Get a Set of all states that occur within the instruction.
         *
         * @return all states occurring within the method
         */
        private Set<RegisterState> getAllStates() {
            final Set<RegisterState> assign = new HashSet<>();

            body.getThisRegister().ifPresent(register -> {
                if (this.register == register) {
                    assign.add(new RegisterState.This(cfgGraph.getHead()));
                }
            });

            int paramIndex = 0;
            for (Register parameterRegister : body.getParameterRegisters()) {
                if (this.register == parameterRegister) {
                    assign.add(new RegisterState.Parameter(cfgGraph.getHead(), paramIndex));
                }
                paramIndex += 1;
            }

            for (Instruction writingInsn : writes) {
                DexCfgGraph.Node node = cfgGraph.getNode(writingInsn);
                assign.add(new RegisterState.Assignment(node));
            }

            return assign;
        }

        /**
         * Walk forward through the CFG starting at a certain node.
         * <p>
         * The function passed as parameter is evaluated for every node on the way.
         * If the lambda evaluates to <tt>false</tt>, then the succeeding nodes will not be visited.
         * <p>
         * Nodes that have already been visited will not be visited again.
         *
         * @param start start the analysis from that node
         * @param visit the function that is evaluated for each visited node
         */
        private void walkCfgForward(DexCfgGraph.Node start, Function<DexCfgGraph.Node, Boolean> visit) {
            Deque<DexCfgGraph.Node> worklist = new ArrayDeque<>();
            Set<DexCfgGraph.Node> visited = new HashSet<>();
            worklist.add(start);

            while (!worklist.isEmpty()) {
                DexCfgGraph.Node node = worklist.poll();

                boolean alreadyVisited = !visited.add(node);
                if (alreadyVisited || !visit.apply(node)) {
                    continue;
                }

                worklist.addAll(node.getSucceeding());
            }

        }

        private void buildAssignGroup() {
            // Map register-writes to the group that they are currently in
            Map<RegisterState, Set<RegisterState>> groupMap = new HashMap<>();

            this.insnToState.forEach((insn, state) -> {
                Iterator<RegisterState> iterator = state.iterator();

                // Get the assignment group that one of the states belongs to or else create a new group.
                Set<RegisterState> insnGroup = groupMap.computeIfAbsent(iterator.next(), firstAssign -> {
                    Set<RegisterState> newGroup = new HashSet<>(Set.of(firstAssign));
                    this.groups.add(newGroup);
                    return newGroup;
                });

                // Ensure that all other states are in the same group
                iterator.forEachRemaining(assign -> {
                    if (!insnGroup.contains(assign)) {
                        Set<RegisterState> oldGroup = groupMap.remove(assign);
                        if (oldGroup == null) {
                            // The assignment is not yet a member of a group, so we add it to the group
                            insnGroup.add(assign);
                            groupMap.put(assign, insnGroup);
                        } else {
                            // The assignment is a member of a different group. This group gets merged with that group.
                            for (RegisterState groupMember : oldGroup) {
                                groupMap.put(groupMember, insnGroup);
                                insnGroup.add(groupMember);
                            }

                            this.groups.remove(oldGroup);
                        }
                    }
                });
            });
        }
    }

    public static interface RegisterState {
        /**
         * Get the Node from whereon this state is assigned to the local.
         *
         * @return node where the assignment of the state occurs
         */
        DexCfgGraph.Node getNode();

        /**
         * The register contains the "this" instance.
         */
        public static class This implements RegisterState {
            private final DexCfgGraph.Node node;

            public This(DexCfgGraph.Node node) {
                this.node = node;
            }

            @Override
            public DexCfgGraph.Node getNode() {
                return node;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                This aThis = (This) o;
                return Objects.equals(node, aThis.node);
            }

            @Override
            public int hashCode() {
                return Objects.hash(node);
            }
        }

        /**
         * The register contains a parameter passed to the method.
         */
        public static class Parameter implements RegisterState {
            private final int index;
            private final DexCfgGraph.Node node;

            public Parameter(DexCfgGraph.Node node, int index) {
                this.node = node;
                this.index = index;
            }

            public int getIndex() {
                return index;
            }

            @Override
            public DexCfgGraph.Node getNode() {
                return node;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Parameter parameter = (Parameter) o;
                return index == parameter.index &&
                        Objects.equals(node, parameter.node);
            }

            @Override
            public int hashCode() {
                return Objects.hash(index, node);
            }
        }

        /**
         * An instruction writes to the register
         */
        public static class Assignment implements RegisterState {
            private final DexCfgGraph.Node node;

            public Assignment(DexCfgGraph.Node node) {
                this.node = node;
            }

            @Override
            public DexCfgGraph.Node getNode() {
                return node;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Assignment that = (Assignment) o;
                return Objects.equals(node, that.node);
            }

            @Override
            public int hashCode() {
                return Objects.hash(node);
            }
        }
    }
}
