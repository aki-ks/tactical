package me.aki.tactical.conversion.smali2dex.typing;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.constant.*;
import me.aki.tactical.core.type.*;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.core.utils.AbstractCfgGraph;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.utils.CommonOperations;
import me.aki.tactical.dex.utils.DexCfgGraph;
import me.aki.tactical.dex.utils.DexInsnReader;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that types Registers and ambiguous instructions.
 *
 * This is accomplished by looking at the types that register expect to read and write.
 */
public class DexTyper {
    private final Method method;
    private final DexBody body;
    private DexCfgGraph cfgGraph;

    private final Map<Instruction, RegisterState> states = new HashMap<>();

    /**
     * All instructions that currently read or write ambiguous values.
     */
    private final Set<AmbiguousInsnInfo<? extends Instruction>> ambiguousInstructions = new HashSet<>();

    public DexTyper(Method method, DexCfgGraph cfgGraph) {
        this.method = method;
        this.body = cfgGraph.getBody();
        this.cfgGraph = cfgGraph;

        computeRegisterStates();
        resolveAmbiguousInstructions();
        setRegisterTypes();
    }

    public RegisterState getStateAt(DexCfgGraph.Node node) {
        return states.computeIfAbsent(node.getInstruction(), RegisterState::new);
    }

    public RegisterState getStateAt(Instruction insn) {
        return states.computeIfAbsent(insn, RegisterState::new);
    }

    /**
     * Update the {@link RegisterState RegisterStates} of all instructions and create a list of all ambiguous instructions.
     */
    private void computeRegisterStates() {
        TypeHintInsnVisitor iv = new TypeHintInsnVisitor(this.method.getReturnType()) {
            @Override
            protected void visit(RegisterAccess access) {
                propagateRegisterState(node, access);

                if (isAmbiguous(access)) {
                    AmbiguousInsnInfo<? extends Instruction> ambiguousInsnInfo = toAmbiguousInsnInfo(node.getInstruction());
                    ambiguousInstructions.add(ambiguousInsnInfo);
                }
            }

            private boolean isAmbiguous(RegisterAccess action) {
                return action.getWrittenType() instanceof AmbiguousType ||
                        action.getReads().values().stream().anyMatch(typ -> typ instanceof AmbiguousType);
            }

            private AmbiguousInsnInfo<? extends Instruction> toAmbiguousInsnInfo(Instruction insn) {
                if (insn instanceof ArrayLoadInstruction) {
                    return new ArrayLoad((ArrayLoadInstruction) insn);
                } else if (insn instanceof ArrayStoreInstruction) {
                    return new ArrayStore((ArrayStoreInstruction) insn);
                } else if (insn instanceof MoveInstruction) {
                    return new Move((MoveInstruction) insn);
                } else if (insn instanceof ConstInstruction) {
                    return new Constant((ConstInstruction) insn);
                } else {
                    return DexUtils.unreachable();
                }
            }
        };

        DexInsnReader reader = new DexInsnReader(iv);
        this.cfgGraph.forEachNode(node -> {
            iv.setNode(node);
            reader.accept(node.getInstruction());
        });
    }

    /**
     * Distribute the read and written types of an instruction through the {@link RegisterState#readTypeInfo} and
     * {@link RegisterState#writtenTypeInfo} of other instructions.
     *
     * @param node the instruction whose read/write information should get propagated
     * @param access the read and written types of the instruction
     */
    public void propagateRegisterState(DexCfgGraph.Node node, TypeHintInsnVisitor.RegisterAccess access) {
        class ReadTypePropagation {
            private void propagateTypeBackwards(DexCfgGraph.Node node, Register register, Type mergeInType) {
                RegisterState state = getStateAt(node);
                state.readTypeInfo.merge(register, mergeInType, this::mergeReadType);

                for (DexCfgGraph.Node predecessor : node.getPreceding()) {
                    Optional<Register> writtenRegister = predecessor.getInstruction().getWrittenRegister();
                    if (writtenRegister.isPresent() && writtenRegister.get() == register) {
                        // The preceding instruction overwrites the value in the register
                        continue;
                    }

                    propagateTypeBackwards(predecessor, register, mergeInType);
                }
            }

            /**
             * Merge two types into one common type.
             *
             * If one type is more ambiguous / less precise than the other type,
             * than that type dominates and is returned.
             *
             * @param a one type
             * @param b another type
             * @return a common supertype of both types
             */
            private Type mergeReadType(Type a, Type b) {
                if (a.equals(b)) {
                    return a;
                }

                if (a instanceof AmbiguousType || b instanceof AmbiguousType) {
                    if (a instanceof AmbiguousType.IntOrFloatOrRef || b instanceof AmbiguousType.IntOrFloatOrRef) {
                        if (isIntOrFloatOrRefType(a) && isIntOrFloatOrRefType(b)) {
                            return AmbiguousType.IntOrFloatOrRef.getInstance();
                        }
                    } else if (a instanceof AmbiguousType.IntOrFloat || b instanceof AmbiguousType.IntOrFloat) {
                        if (isIntOrFloatType(a) && isIntOrFloatType(b)) {
                            return AmbiguousType.IntOrFloat.getInstance();
                        }
                    } else if (a instanceof AmbiguousType.LongOrDouble || b instanceof AmbiguousType.LongOrDouble) {
                        if (isLongOrDoubleType(a) && isLongOrDoubleType(b)) {
                            return AmbiguousType.LongOrDouble.getInstance();
                        }
                    } else {
                        return DexUtils.unreachable();
                    }
                } else {
                    if (a instanceof IntLikeType && b instanceof IntLikeType) {
                        return IntType.getInstance();
                    } else if (a instanceof RefType && b instanceof RefType) {
                        return ObjectType.OBJECT;
                    }
                }

                throw new IllegalStateException("Cannot merge read of type " + a + " and " + b);
            }
        }

        class WrittenTypePropagation {
            private void propagateForward(DexCfgGraph.Node node, Register register, Type type) {
                RegisterState state = getStateAt(node);

                state.writtenTypeInfo.merge(register, type, this::mergeWrittenType);

                for (DexCfgGraph.Node succeeding : node.getSucceeding()) {
                    Optional<Register> writtenRegister = succeeding.getInstruction().getWrittenRegister();
                    if (writtenRegister.isPresent() && writtenRegister.get() == register) {
                        // The next overwrites the value in the register
                        continue;
                    }

                    // Types that will be in the register coming from all possible branches
                    List<Type> previousTypes = succeeding.getPreceding().stream()
                            .map(n -> getStateAt(n).writtenTypeInfo.get(register))
                            .collect(Collectors.toList());

                    if (previousTypes.contains(null)) {
                        // Not all branches from which this instruction is reachable have written
                        // to the register or the type comping from that branch was not yet computed.
                    } else {
                        Type mergedType = previousTypes.stream().reduce(this::mergeWrittenType).get();
                        propagateForward(node, register, mergedType);
                    }
                }
            }

            /**
             * Merge two types into one common type.
             */
            private Type mergeWrittenType(Type typeA, Type typeB) {
                if (typeA instanceof TypeConflict || typeB instanceof TypeConflict) {
                    return TypeConflict.INSTANCE;
                }

                if (typeA instanceof AmbiguousType) {
                    return mergeWrittenAmbiguousType((AmbiguousType) typeA, typeB);
                } else if (typeB instanceof AmbiguousType) {
                    return mergeWrittenAmbiguousType((AmbiguousType) typeB, typeA);
                } else {
                    if (typeA.equals(typeB)) {
                        return typeA;
                    } else if (typeA instanceof IntLikeType && typeB instanceof IntLikeType) {
                        return IntType.getInstance();
                    } else if (typeA instanceof RefType && typeB instanceof RefType) {
                        return ObjectType.OBJECT;
                    }
                }

                return TypeConflict.INSTANCE;
            }

            /**
             * Merge an ambiguous Type with another type.
             * More precise types dominate.
             *
             * If one type is more precise / less ambiguous than the other type,
             * than that type dominates and is returned.
             *
             * @param ambiguousType an ambiguous type
             * @param type type to be merged in
             * @return the more precise type of both
             */
            private Type mergeWrittenAmbiguousType(AmbiguousType ambiguousType, Type type) {
                if (ambiguousType instanceof AmbiguousType.LongOrDouble) {
                    if (isLongOrDoubleType(type)) {
                        return type;
                    }
                } else if (ambiguousType instanceof AmbiguousType.IntOrFloat) {
                    if (type instanceof AmbiguousType.IntOrFloatOrRef) {
                        // IntOrFloatOrRef is a more imprecise type than IntOrFloat.
                        // This method should return the most precise of both types.
                        return AmbiguousType.IntOrFloat.getInstance();
                    }
                    if (isIntOrFloatType(type)) {
                        return type;
                    }
                } else if (ambiguousType instanceof AmbiguousType.IntOrFloatOrRef) {
                    if (isIntOrFloatOrRefType(type)) {
                        return type;
                    }
                } else {
                    return DexUtils.unreachable();
                }

                // We've got a merge conflict
                return null;
            }
        }

        ReadTypePropagation readPropagation = new ReadTypePropagation();
        access.getReads().forEach((register, type) ->
                readPropagation.propagateTypeBackwards(node, register, type));

        if (access.getWrittenRegister() != null) {
            WrittenTypePropagation writePropagation = new WrittenTypePropagation();
            writePropagation.propagateForward(node, access.getWrittenRegister(), access.getWrittenType());
        }
    }

    private boolean isIntOrFloatType(Type type) {
        return type instanceof AmbiguousType.IntOrFloat || type instanceof IntLikeType || type instanceof FloatType;
    }

    private boolean isIntOrFloatOrRefType(Type type) {
        return type instanceof AmbiguousType.IntOrFloatOrRef || type instanceof AmbiguousType.IntOrFloat ||
                type instanceof IntLikeType || type instanceof FloatType || type instanceof RefType;
    }

    private boolean isLongOrDoubleType(Type type) {
        return type instanceof AmbiguousType.LongOrDouble || type instanceof LongType || type instanceof DoubleType;
    }

    private void resolveAmbiguousInstructions() {
        int ambiguousInsnsBefore = this.ambiguousInstructions.size();

        this.ambiguousInstructions.removeIf(ambiguousInstruction -> {
            boolean wasTypeComputed = ambiguousInstruction.tryTypeComputation();
            if (wasTypeComputed) {
                AbstractCfgGraph<Instruction>.Node node = cfgGraph.getNode(ambiguousInstruction.instruction);
                propagateRegisterState(node, recomputeRegisterAccess(node));
            }
            return wasTypeComputed;
        });

        if (!this.ambiguousInstructions.isEmpty()) {
            if (ambiguousInsnsBefore == this.ambiguousInstructions.size()) {
                removeUnusedAmbiguousMoves();
                removeUnusedAmbiguousConstInstructions();

                if (!ambiguousInstructions.isEmpty()) {
                    throw new RuntimeException("Could not type all instructions of ambiguous types");
                }
            } else {
                // Some ambiguous instructions have been typed.
                // Since there is now more type information available,
                // we can probably resolve the types remaining ambiguous instructions
                resolveAmbiguousInstructions();
            }
        }
    }

    /**
     * If a {@link MoveInstruction} moves the value of an ambiguous {@link ConstInstruction}
     * to a register whose value is not read, then neither instruction can be typed.
     * Since the {@link MoveInstruction} is useless, we will just remove it.
     *
     * We must also consider the case where another {@link MoveInstruction} moves the result of the
     * previous {@link MoveInstruction} to another unused register.
     *
     * We must handle complicated cases such as this:
     * <pre><code>
     *     x = [some ambiguous value];
     *
     * foo:
     *     y = x;
     *     z = y;
     *     x = z;
     *     goto foo;
     * </code></pre>
     */
    private void removeUnusedAmbiguousMoves() {
        Set<AmbiguousInsnInfo<? extends Instruction>> unusedMoves = new HashSet<>();
        Set<MoveInstruction> unusedMoveInstructions = new HashSet<>();

        class IsUnusedMoveAnalysis {
            private final Set<DexCfgGraph.Node> visited = new HashSet<>();
            private final boolean isUnused;

            public IsUnusedMoveAnalysis(MoveInstruction instruction) {
                this.isUnused = isUnused(cfgGraph.getNode(instruction), instruction);
            }

            private boolean isUnused(DexCfgGraph.Node node, MoveInstruction instruction) {
                if (!visited.add(node) || unusedMoveInstructions.contains(instruction)) {
                    return true;
                }

                Set<DexCfgGraph.Node> readingInsnNodes = getReadingRegisters(node, instruction.getTo());
                for (DexCfgGraph.Node readingInsnNode : readingInsnNodes) {
                    Instruction readingInsn = readingInsnNode.getInstruction();
                    if (readingInsn instanceof MoveInstruction) {
                        if (!isUnused(node, instruction)) {
                            return false;
                        }
                    } else {
                        // The only instructions that are possibly not yet typed are constant and move instructions.
                        // Since constant instructions do not read from registers, this may only be a move instruction.
                        throw new IllegalStateException();
                    }
                }
                return true;
            }
        }

        for (AmbiguousInsnInfo<? extends Instruction> ambiguousInsn : this.ambiguousInstructions) {
            if (ambiguousInsn.instruction instanceof MoveInstruction) {
                MoveInstruction moveInsn = (MoveInstruction) ambiguousInsn.instruction;
                if (new IsUnusedMoveAnalysis(moveInsn).isUnused) {
                    unusedMoves.add(ambiguousInsn);
                    unusedMoveInstructions.add(moveInsn);
                }
            }
        }

        InsertList<Instruction> insnList = this.body.getInstructions();
        unusedMoves.forEach(ambiguousMove -> {
            ambiguousInstructions.remove(ambiguousMove);
            insnList.remove(ambiguousMove.instruction);
        });
    }

    /**
     * Find all registers that read the value that a certain instruction wrote in a register.
     *
     * @param node the instruction that wrote to the register
     * @param register the register that the instruction wrote to
     * @return all instructions that may possibly read the written value
     */
    private Set<DexCfgGraph.Node> getReadingRegisters(DexCfgGraph.Node node, Register register) {
        class Inner {
            private final Set<DexCfgGraph.Node> reads = new HashSet<>();
            private final Set<DexCfgGraph.Node> visited = new HashSet<>();

            private void addSucceedingReads(DexCfgGraph.Node node) {
                node.getSucceeding().forEach(this::addReadsOfThisOrSuceedingNode);
            }

            private void addReadsOfThisOrSuceedingNode(DexCfgGraph.Node node) {
                if (!this.visited.add(node)) {
                    return;
                }

                if (node.getInstruction().getReadRegisters().contains(register)) {
                    reads.add(node);
                }

                Optional<Register> writeOpt = node.getInstruction().getWrittenRegister();
                if (writeOpt.isPresent() && writeOpt.get() == register) {
                    // The value in the register is overwritten by this instruction,
                    // so succeeding instructions cannot read the values.
                } else {
                    addSucceedingReads(node);
                }
            }
        }

        Inner inner = new Inner();
        inner.addSucceedingReads(node);
        return inner.reads;
    }

    /**
     * If a {@link ConstInstruction} stores a value of an ambiguous type in a
     * register that is not read afterwards, it is not possible to compute the type.
     * Since such an instruction has no side effects, we can just remove it.
     */
    private void removeUnusedAmbiguousConstInstructions() {
        class IsReadCheck {
            private final Set<DexCfgGraph.Node> visited = new HashSet<>();
            private final Register register;
            private final boolean isRead;

            /**
             * Check whether the value that is written by an instruction into a register is later possibly read.
             *
             * @param node instruction that writes a value into the register
             * @param register the instruction writes a value into this register
             */
            private IsReadCheck(DexCfgGraph.Node node, Register register) {
                this.register = register;
                this.isRead = isReadBySuccessors(node);
            }

            private boolean isReadByNode(DexCfgGraph.Node node) {
                if (!this.visited.add(node)) {
                    return false;
                }

                if (!body.getInstructions().contains(node.getInstruction())) {
                    // The 'removeUnusedAmbiguousMoves' method may remove instructions
                    // but does not update the cfg, so we skip those instruction.
                    return isReadBySuccessors(node);
                }

                if (node.getInstruction().getReadRegisters().contains(this.register)) {
                    return true;
                }

                Optional<Register> writeOpt = node.getInstruction().getWrittenRegister();
                if (writeOpt.isPresent() && writeOpt.get() == this.register) {
                    // The value in the register is overwritten,
                    // so succeeding instructions cannot read the values.
                    return false;
                } else {
                    return isReadBySuccessors(node);
                }
            }

            private boolean isReadBySuccessors(DexCfgGraph.Node node) {
                return node.getSucceeding().stream().anyMatch(this::isReadByNode);
            }

            public boolean isRead() {
                return isRead;
            }
        }

        this.ambiguousInstructions.removeIf(ambiguousInsn -> {
            Instruction insn = ambiguousInsn.getInstruction();
            if (insn instanceof ConstInstruction) {
                Register register = ((ConstInstruction) insn).getRegister();
                DexCfgGraph.Node node = this.cfgGraph.getNode(ambiguousInsn.instruction);
                return !new IsReadCheck(node, register).isRead();
            } else {
                return false;
            }
        });
    }

    /**
     * Compute the types that an instruction reads and writes.
     *
     * @param node instruction whose reads and writes should be computed
     * @return the computed reads and write types
     */
    private TypeHintInsnVisitor.RegisterAccess recomputeRegisterAccess(DexCfgGraph.Node node) {
        RWCell<TypeHintInsnVisitor.RegisterAccess> result = RWCell.of(null, TypeHintInsnVisitor.RegisterAccess.class);

        TypeHintInsnVisitor iv = new TypeHintInsnVisitor(this.method.getReturnType()) {
            @Override
            protected void visit(RegisterAccess access) {
                result.set(access);
            }
        };

        DexInsnReader reader = new DexInsnReader(iv);
        iv.setNode(node);
        reader.accept(node.getInstruction());

        return Objects.requireNonNull(result.get());
    }

    /**
     * Assign types to all registers or remove them if they are unused.
     *
     * This is done by looking at all reads, writes and if it is a parameter/this register at that types.
     * All these types are merged together. If the {@link RegisterPartitioner} was ran,
     * this should not possibly create a merge conflict.
     */
    private void setRegisterTypes() {
        Map<Register, Set<Instruction>> readMap = CommonOperations.getReadMap(this.body);
        Map<Register, Set<Instruction>> writeMap = CommonOperations.getWriteMap(this.body);

        body.getRegisters().removeIf(register -> {
            Stream<Type> readTypes = readMap.getOrDefault(register, Set.of()).stream().map(insn -> getStateAt(insn).readTypeInfo.get(register));
            Stream<Type> writtenTypes = writeMap.getOrDefault(register, Set.of()).stream().map(insn -> getStateAt(insn).writtenTypeInfo.get(register));
            Optional<Type> registerType = Stream.concat(getRequiredType(register).stream(), Stream.concat(readTypes, writtenTypes)).reduce(this::mergeType);

            if (registerType.isPresent()) {
                register.setType(registerType.get());
                return false;
            } else {
                // The register neither used nor is it a parameter or this register.
                return true;
            }
        });
    }

    private Type mergeType(Type typeA, Type typeB) {
        if (typeA instanceof AmbiguousType || typeB instanceof AmbiguousType ||
                typeA instanceof TypeConflict || typeB instanceof TypeConflict) {
            // Ambiguous types should actually be resolved here.
            throw new IllegalStateException();
        }

        if (typeA.equals(typeB)) {
            return typeA;
        } else if (typeA instanceof IntLikeType && typeB instanceof IntLikeType) {
            return IntType.getInstance();
        } else if (typeA instanceof RefType && typeB instanceof RefType) {
            return ObjectType.OBJECT;
        } else {
            throw new IllegalArgumentException("Cannot merge types " + typeA + " and " + typeB);
        }
    }

    private Optional<Type> getRequiredType(Register register) {
        Optional<Register> thisRegister = body.getThisRegister();
        if (thisRegister.isPresent() && thisRegister.get() == register) {
            return Optional.of(ObjectType.OBJECT);
        }

        Iterator<Register> iterator = body.getParameterRegisters().iterator();
        for (Type parameterType : method.getParameterTypes()) {
            Register parameterRegister = iterator.next();
            if (parameterRegister == register) {
                return Optional.of(parameterType);
            }
        }

        return Optional.empty();
    }

    /**
     * A snapshot of the types that the registers will have at a certain location in code.
     */
    public class RegisterState {
        /**
         * This snapshot contains the register states at this location in code.
         */
        private final Instruction instruction;

        /**
         * Types that the values in the registers must have before the execution of the instruction.
         *
         * This information is only derived from read from registers.
         */
        private final Map<Register, Type> readTypeInfo = new HashMap<>();

        /**
         * Types that the values in the registers will have after the execution of the instruction.
         *
         * This information is only derived from writes to the registers.
         */
        private final Map<Register, Type> writtenTypeInfo = new HashMap<>();

        public RegisterState(Instruction instruction) {
            this.instruction = instruction;
        }
    }

    private abstract class AmbiguousInsnInfo<I extends Instruction> {
        protected final I instruction;

        protected AmbiguousInsnInfo(I instruction) {
            this.instruction = instruction;
        }

        public I getInstruction() {
            return instruction;
        }

        public abstract boolean tryTypeComputation();

        /**
         * Try to compute the type that this instruction will read from a register as precise as possible
         *
         * We therefore look at the they that this instruction read from the register and
         * the types that the preceding instructions have written to the register.
         *
         * @param register a register that this instruction reads from
         * @return type of the value read from the register
         */
        protected Type getReadType(Register register) {
            DexCfgGraph.Node currentNode = cfgGraph.getNode(instruction);
            Type typeReadByInsn = getStateAt(currentNode).readTypeInfo.get(register);

            return currentNode.getPreceding().stream()
                    .map(node -> getStateAt(node).writtenTypeInfo.get(register))
                    .map(this::requireNoTypeConflict)
                    .reduce(typeReadByInsn, this::mergeTypes);
        }

        private Type requireNoTypeConflict(Type type) {
            if (type instanceof TypeConflict) {
                throw new IllegalStateException("Tried to read from register with a TypeConflict");
            } else {
                return type;
            }
        }

        /**
         * Try to compute the type that this instruction writes as precise as possible.
         *
         * We therefore look at the type that this instruction writes and
         * the types that the succeeding instructions expect to read.
         *
         * @param register the register that the insn writes into
         * @return type of the value written to the register
         */
        protected Type getWrittenType(Register register) {
            DexCfgGraph.Node currentNode = cfgGraph.getNode(instruction);
            Type typeWrittenByInsn = requireNoTypeConflict(getStateAt(currentNode).writtenTypeInfo.get(register));

            return currentNode.getSucceeding().stream()
                    .map(node -> getStateAt(node).readTypeInfo.get(register))
                    .filter(Objects::nonNull) // filter branches that do not read from the register
                    .reduce(typeWrittenByInsn, this::mergeTypes);
        }

        protected Type mergeTypes(Type typeA, Type typeB) {
            if (typeA instanceof AmbiguousType) return mergeWithAmbiguousType((AmbiguousType) typeA, typeB);
            if (typeB instanceof AmbiguousType) return mergeWithAmbiguousType((AmbiguousType) typeB, typeA);

            if (typeA.equals(typeB)) {
                return typeA;
            } else if (typeA instanceof IntLikeType && typeB instanceof IntLikeType) {
                return IntType.getInstance();
            } else if (typeA instanceof RefType && typeB instanceof RefType) {
                return ObjectType.OBJECT;
            } else {
                throw new IllegalArgumentException("Cannot merge types " + typeA + " and " + typeB);
            }
        }

        private Type mergeWithAmbiguousType(AmbiguousType typeA, Type typeB) {
            if (typeA instanceof AmbiguousType.IntOrFloat) {
                if (isIntOrFloatType(typeB)) {
                    return typeB;
                }
            } else if (typeA instanceof AmbiguousType.IntOrFloatOrRef) {
                if (isIntOrFloatOrRefType(typeB)) {
                    return typeB;
                }
            } else if (typeA instanceof AmbiguousType.LongOrDouble) {
                if (isLongOrDoubleType(typeB)) {
                    return typeB;
                }
            } else {
                return DexUtils.unreachable();
            }

            throw new IllegalStateException("Cannot merge " + typeA + " with " + typeB);
        }

        protected Type getLowerType(Type readType) {
            if (readType instanceof AmbiguousType) {
                return null;
            } else if (readType instanceof ArrayType) {
                Type lowerType = ((ArrayType) readType).getLowerType();
                if (lowerType instanceof PrimitiveType) {
                    return lowerType;
                } else {
                    // If the array is multi-dimensional or has a reference base-type,
                    // then we would have statically known that it reads from a RefType.
                    // The instruction should therefore not be ambiguous.
                    throw new IllegalStateException("Expected one dimensional array of a primitive type");
                }
            } else if (readType instanceof RefType) {
                // This is most likely invalid bytecode, since it must be proven to the
                // bytecode verifier that we really read from an array type.
                return null;
            } else if (readType instanceof PrimitiveType) {
                throw new IllegalStateException("Expected array type, found primitive type");
            } else {
                return DexUtils.unreachable();
            }
        }
    }

    private class ArrayLoad extends AmbiguousInsnInfo<ArrayLoadInstruction> {
        public ArrayLoad(ArrayLoadInstruction insn) {
            super(insn);
        }

        @Override
        public boolean tryTypeComputation() {
            Type arrayBaseType = getLowerType(getReadType(instruction.getArray()));
            Type valueType = getWrittenType(instruction.getResult());

            Type type = mergeTypes(arrayBaseType, valueType);
            if (type instanceof AmbiguousType) {
                return false;
            } else {
                instruction.setType(type);
                return true;
            }
        }
    }

    private class ArrayStore extends AmbiguousInsnInfo<ArrayStoreInstruction> {
        public ArrayStore(ArrayStoreInstruction insn) {
            super(insn);
        }

        @Override
        public boolean tryTypeComputation() {
            Type arrayBaseType = getLowerType(getReadType(instruction.getArray()));
            Type valueType = getReadType(instruction.getValue());

            Type type = mergeTypes(arrayBaseType, valueType);
            if (type instanceof AmbiguousType) {
                return false;
            } else {
                instruction.setType(type);
                return true;
            }
        }
    }

    private class Move extends AmbiguousInsnInfo<MoveInstruction> {
        public Move(MoveInstruction insn) {
            super(insn);
        }

        @Override
        public boolean tryTypeComputation() {
            Type type = mergeTypes(getReadType(instruction.getFrom()), getWrittenType(instruction.getTo()));
            if (type instanceof AmbiguousType) {
                return false;
            } else {
                instruction.setType(type);
                return true;
            }
        }
    }

    private class Constant extends AmbiguousInsnInfo<ConstInstruction> {
        public Constant(ConstInstruction insn) {
            super(insn);
        }

        @Override
        public boolean tryTypeComputation() {
            Type type = getWrittenType(instruction.getRegister());
            if (type instanceof AmbiguousType) {
                return false;
            } else {
                instruction.setConstant(toConstant((UntypedNumberConstant) instruction.getConstant(), type));
                return true;
            }
        }

        private DexConstant toConstant(UntypedNumberConstant constant, Type type) {
            if (type instanceof IntLikeType) {
                return new IntConstant(constant.intValue());
            } else if (type instanceof LongType) {
                return new LongConstant(constant.longValue());
            } else if (type instanceof FloatType) {
                return new FloatConstant(constant.floatValue());
            } else if (type instanceof DoubleType) {
                return new DoubleConstant(constant.doubleValue());
            } else if (type instanceof RefType) {
                if (constant.longValue() == 0L) {
                    return NullConstant.getInstance();
                } else {
                    throw new RuntimeException("Cannot convert constant " + constant + " to a referene type");
                }
            } else {
                return DexUtils.unreachable();
            }
        }
    }

    private static class TypeConflict implements Type {
        private static final TypeConflict INSTANCE = new TypeConflict();

        private TypeConflict() {}

        @Override
        public String toString() {
            return TypeConflict.class.getSimpleName() + "{}";
        }
    }
}
