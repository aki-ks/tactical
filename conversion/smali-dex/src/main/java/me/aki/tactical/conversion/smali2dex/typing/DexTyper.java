package me.aki.tactical.conversion.smali2dex.typing;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.constant.*;
import me.aki.tactical.core.type.*;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.*;
import me.aki.tactical.dex.utils.CommonOperations;
import me.aki.tactical.dex.utils.DexCfgGraph;
import me.aki.tactical.dex.utils.DexInsnReader;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that types Registers and ambiguous instructions.
 *
 * This is accomplished by looking at the types that register expect to read and write.
 */
public class DexTyper {
    private final TypeMerge typeMerge = new TypeMerge();

    private final Method method;
    private final DexBody body;
    private final DexCfgGraph cfgGraph;

    private final Map<Instruction, RegisterState> states = new HashMap<>();

    /**
     * All instructions that currently read or write ambiguous values.
     */
    private final Set<AmbiguousInsnInfo<? extends Instruction>> ambiguousInstructions = new HashSet<>();

    public DexTyper(Method method, DexCfgGraph cfgGraph) {
        this.method = method;
        this.body = cfgGraph.getBody();
        this.cfgGraph = cfgGraph;

        createInitialRegisterStates();
        propagateRegisterAccesses();
    }

    public RegisterState getStateAt(Instruction insn) {
        return states.get(insn);
    }

    /**
     * Create a {@link RegisterState} with the computed {@link RegisterState#access} for each instruction.
     * The {@link RegisterState#reads} and {@link RegisterState#writes} are not yet computed.
     */
    private void createInitialRegisterStates() {
        TypeHintInsnVisitor iv = new TypeHintInsnVisitor(this.method.getReturnType()) {
            @Override
            protected void visit(RegisterAccess access) {
                states.put(node.getInstruction(), new RegisterState(node, access));

                if (isAmbiguous(access)) {
                    AmbiguousInsnInfo<? extends Instruction> ambiguousInsnInfo = toAmbiguousInsnInfo(node);
                    ambiguousInstructions.add(ambiguousInsnInfo);
                }
            }

            private boolean isAmbiguous(RegisterAccess access) {
                return access.getWrittenType() instanceof AmbiguousType ||
                        access.getReads().values().stream().anyMatch(typ -> typ instanceof AmbiguousType);
            }

            private AmbiguousInsnInfo<? extends Instruction> toAmbiguousInsnInfo(DexCfgGraph.Node node) {
                Instruction insn = node.getInstruction();
                if (insn instanceof ArrayLoadInstruction) {
                    return new ArrayLoad(node, (ArrayLoadInstruction) insn);
                } else if (insn instanceof ArrayStoreInstruction) {
                    return new ArrayStore(node, (ArrayStoreInstruction) insn);
                } else if (insn instanceof MoveInstruction) {
                    return new Move(node, (MoveInstruction) insn);
                } else if (insn instanceof ConstInstruction) {
                    return new Constant(node, (ConstInstruction) insn);
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
     * Distribute the information that an instruction reads or writes from/to a register through the
     * {@link RegisterState#reads} and {@link RegisterState#writes} of all affected instructions.
     */
    public void propagateRegisterAccesses() {
        class TypePropagation {
            private void propagateReadBackwards(DexCfgGraph.Node node, Instruction readingInsn, Register register) {
                RegisterState state = getStateAt(node.getInstruction());
                Set<Instruction> reads = state.reads.computeIfAbsent(register, x -> new HashSet<>());
                if (!reads.add(readingInsn)) {
                    // We've already propagated this read to this instruction.
                    // This occurs if we propagate a type through some kind of loop.
                    return;
                }

                for (DexCfgGraph.Node predecessor : node.getPreceding()) {
                    Optional<Register> writtenRegister = predecessor.getInstruction().getWrittenRegister();
                    if (writtenRegister.isPresent() && writtenRegister.get() == register) {
                        // The preceding instruction overwrites the value in the register
                        continue;
                    }

                    propagateReadBackwards(predecessor, readingInsn, register);
                }
            }

            private void propagateWriteForward(DexCfgGraph.Node node, Instruction writingInsn, Register register) {
                RegisterState state = getStateAt(node.getInstruction());

                Set<Instruction> writes = state.writes.computeIfAbsent(register, x -> new HashSet<>());
                if (!writes.add(writingInsn)) {
                    // We've already propagated this write to this instruction.
                    // This may happen if we went through a loop.
                    return;
                }

                for (DexCfgGraph.Node succeeding : node.getSucceeding()) {
                    Optional<Register> writtenRegister = succeeding.getInstruction().getWrittenRegister();
                    if (writtenRegister.isPresent() && writtenRegister.get() == register) {
                        // The next overwrites the value in the register
                        continue;
                    }

                    propagateWriteForward(succeeding, writingInsn, register);
                }
            }
        }

        TypePropagation typePropagation = new TypePropagation();

        for (RegisterState state : states.values()) {
            TypeHintInsnVisitor.RegisterAccess access = state.access;

            access.getReads().forEach((register, type) -> {
                typePropagation.propagateReadBackwards(state.node, state.node.getInstruction(), register);
            });

            if (access.getWrittenRegister() != null) {
                typePropagation.propagateWriteForward(state.node, state.node.getInstruction(), access.getWrittenRegister());
            }
        }
    }

    public void typeInstructions() {
        int ambiguousInsnsBefore = this.ambiguousInstructions.size();

        this.ambiguousInstructions.removeIf(ambiguousInstruction -> {
            boolean wasTypeComputed = ambiguousInstruction.tryTypeComputation();
            if (wasTypeComputed) {
                DexCfgGraph.Node node = ambiguousInstruction.node;
                getStateAt(node.getInstruction()).access = recomputeRegisterAccess(node);
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
                // we can probably resolve the remaining ambiguous instructions
                typeInstructions();
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
    @SuppressWarnings("unchecked")
    private void removeUnusedAmbiguousMoves() {
        Set<AmbiguousInsnInfo<? extends MoveInstruction>> unusedMoves = new HashSet<>();
        Set<MoveInstruction> unusedMoveInsns = new HashSet<>();

        class IsUnusedMoveAnalysis {
            private final Set<DexCfgGraph.Node> visited = new HashSet<>();
            private final boolean isUnused;

            public IsUnusedMoveAnalysis(AmbiguousInsnInfo<? extends MoveInstruction> ambiguousInsn) {
                this.isUnused = isUnused(ambiguousInsn.node, ambiguousInsn.instruction);
            }

            private boolean isUnused(DexCfgGraph.Node node, MoveInstruction instruction) {
                if (!visited.add(node) || unusedMoveInsns.contains(instruction)) {
                    return true;
                }

                for (DexCfgGraph.Node readingInsn : getReadingRegisters(node, instruction.getTo())) {
                    if (readingInsn.getInstruction() instanceof MoveInstruction) {
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
                AmbiguousInsnInfo<? extends MoveInstruction> moveInsn = (AmbiguousInsnInfo<? extends MoveInstruction>) ambiguousInsn;
                if (new IsUnusedMoveAnalysis(moveInsn).isUnused) {
                    unusedMoves.add(moveInsn);
                    unusedMoveInsns.add(moveInsn.instruction);
                }
            }
        }

        unusedMoves.forEach(ambiguousMove -> {
            ambiguousInstructions.remove(ambiguousMove);
            removeNode(ambiguousMove);
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
            if (ambiguousInsn.getInstruction() instanceof ConstInstruction) {
                ConstInstruction instruction = (ConstInstruction) ambiguousInsn.getInstruction();
                if (!new IsReadCheck(ambiguousInsn.node, instruction.getRegister()).isRead()) {
                    removeNode(ambiguousInsn);
                    return true;
                }
            }
            return false;
        });
    }

    private void removeNode(AmbiguousInsnInfo<? extends Instruction> ambiguousInsn) {
        states.remove(ambiguousInsn.instruction);
        cfgGraph.remove(ambiguousInsn.node);
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
    public void typeRegisters() {
        Map<Register, Set<Instruction>> readMap = CommonOperations.getReadMap(this.body);
        Map<Register, Set<Instruction>> writeMap = CommonOperations.getWriteMap(this.body);

        this.body.getRegisters().removeIf(register -> {
            Stream<Type> readTypes = readMap.getOrDefault(register, Set.of()).stream().flatMap(insn -> getStateAt(insn).getReadType(register).stream());
            Stream<Type> writtenTypes = writeMap.getOrDefault(register, Set.of()).stream().map(insn -> getStateAt(insn).getWrittenType(register));
            Stream<Type> derivedType = deriveTypeFromRegister(register).stream();

            Optional<Type> mergedType = Stream.of(readTypes, writtenTypes, derivedType)
                    .reduce(Stream.empty(), Stream::concat)
                    .reduce(this.typeMerge::mergePreciseTypes);

            if (mergedType.isPresent()) {
                register.setType(mergedType.get());
                return false;
            } else {
                // The register is neither used nor is it a parameter or this register, so it can safely be removed.
                return true;
            }
        });
    }

    private Optional<Type> deriveTypeFromRegister(Register register) {
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
        private final DexCfgGraph.Node node;

        private TypeHintInsnVisitor.RegisterAccess access;

        /**
         * Map registers to all instructions that may possible read the value that is currently
         * (when the instruction is executed) present in the register.
         */
        private final Map<Register, Set<Instruction>> reads = new HashMap<>();

        /**
         * Map registers to all instructions that may possibly have written the value that is currently
         * (when the instruction is executed) present in the register.
         */
        private final Map<Register, Set<Instruction>> writes = new HashMap<>();

        public RegisterState(DexCfgGraph.Node node, TypeHintInsnVisitor.RegisterAccess access) {
            this.node = node;
            this.access = access;
        }

        public Optional<Type> getReadType(Register register) {
            return this.reads.getOrDefault(register, Set.of()).stream()
                .map(insn -> getReadTypeAt(register, insn))
                .reduce(typeMerge::mergeAmbiguousDominating);
        }

        private Type getReadTypeAt(Register register, Instruction insn) {
            Type type = getStateAt(insn).access.getReads().get(register);
            if (type == null) {
                throw new IllegalStateException("Instruction " + insn + " does not write to register " + register);
            } else {
                return type;
            }
        }

        public Type getWrittenType(Register register) {
            return this.writes.getOrDefault(register, Set.of()).stream()
                    .map(insn -> getWrittenTypeAt(insn, register))
                    .reduce(typeMerge::mergePreciseDominating)
                    .orElseThrow(() -> new IllegalStateException("Register " + register + " is never read before instruction " + node.getInstruction()));
        }

        private Type getWrittenTypeAt(Instruction insn, Register register) {
            TypeHintInsnVisitor.RegisterAccess access = getStateAt(insn).access;
            if (access.getWrittenRegister() == register) {
                return access.getWrittenType();
            } else {
                throw new IllegalStateException("Instruction " + insn + " does not read from register " + register);
            }
        }
    }

    private abstract class AmbiguousInsnInfo<I extends Instruction> {
        protected final DexCfgGraph.Node node;
        protected final I instruction;

        protected AmbiguousInsnInfo(DexCfgGraph.Node node, I instruction) {
            if (node.getInstruction() != instruction) {
                throw new IllegalArgumentException();
            }

            this.node = node;
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
            Optional<Type> typeFromRead = getStateAt(node.getInstruction()).getReadType(register);

            Type mergedWrittenTypes = node.getPreceding().stream()
                    .map(node -> getStateAt(node.getInstruction()).getWrittenType(register))
                    .reduce(typeMerge::mergePreciseDominating)
                    .orElseThrow(() -> new IllegalStateException("No instruction has possibly written to a register that is read from"));

            Type mergedType = typeFromRead.stream().reduce(mergedWrittenTypes, typeMerge::mergePreciseDominating);

            return requireNoTypeConflict(mergedType);
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
            Type typeWrittenByInsn = requireNoTypeConflict(getStateAt(node.getInstruction()).getWrittenType(register));

            Type mergedType = node.getSucceeding().stream()
                    .flatMap(node -> getStateAt(node.getInstruction()).getReadType(register).stream())
                    .reduce(typeWrittenByInsn, typeMerge::mergePreciseDominating);

            return requireNoTypeConflict(mergedType);
        }

        private Type requireNoTypeConflict(Type type) {
            if (type instanceof TypeConflict) {
                throw new IllegalStateException("Tried to read from register with a TypeConflict");
            } else {
                return type;
            }
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
        public ArrayLoad(DexCfgGraph.Node node, ArrayLoadInstruction insn) {
            super(node, insn);
        }

        @Override
        public boolean tryTypeComputation() {
            Type arrayBaseType = getLowerType(getReadType(instruction.getArray()));
            Type valueType = getWrittenType(instruction.getResult());

            Type type = typeMerge.mergePreciseDominating(arrayBaseType, valueType);
            if (type instanceof AmbiguousType) {
                return false;
            } else {
                instruction.setType(type);
                return true;
            }
        }
    }

    private class ArrayStore extends AmbiguousInsnInfo<ArrayStoreInstruction> {
        public ArrayStore(DexCfgGraph.Node node, ArrayStoreInstruction insn) {
            super(node, insn);
        }

        @Override
        public boolean tryTypeComputation() {
            Type arrayBaseType = getLowerType(getReadType(instruction.getArray()));
            Type valueType = getReadType(instruction.getValue());

            Type type = typeMerge.mergePreciseDominating(arrayBaseType, valueType);
            if (type instanceof AmbiguousType) {
                return false;
            } else {
                instruction.setType(type);
                return true;
            }
        }
    }

    private class Move extends AmbiguousInsnInfo<MoveInstruction> {
        public Move(DexCfgGraph.Node node, MoveInstruction insn) {
            super(node, insn);
        }

        @Override
        public boolean tryTypeComputation() {
            Type fromType = getReadType(instruction.getFrom());
            Type toType = getWrittenType(instruction.getTo());

            Type type = typeMerge.mergePreciseDominating(fromType, toType);
            if (type instanceof AmbiguousType) {
                return false;
            } else {
                instruction.setType(type);
                return true;
            }
        }
    }

    private class Constant extends AmbiguousInsnInfo<ConstInstruction> {
        public Constant(DexCfgGraph.Node node, ConstInstruction insn) {
            super(node, insn);
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
                    throw new RuntimeException("Cannot convert constant " + constant + " to a reference type");
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

    /**
     * A utility for merging two types into one single type.
     */
    private static class TypeMerge {
        /**
         * Merge two types into one common type.
         *
         * If one type is more ambiguous / less precise than the other type,
         * than that type dominates and is returned.
         *
         * If two precise types are merged, they may turn into an ambiguous type.
         *
         * @param typeA one type
         * @param typeB another type
         * @return the merged type
         */
        public Type mergeAmbiguousDominating(Type typeA, Type typeB) {
            if (typeA.equals(typeB)) {
                return typeA;
            }

            if (typeA instanceof AmbiguousType || typeB instanceof AmbiguousType) {
                if (typeA instanceof AmbiguousType.IntOrFloatOrRef || typeB instanceof AmbiguousType.IntOrFloatOrRef) {
                    if (isIntOrFloatOrRefType(typeA) && isIntOrFloatOrRefType(typeB)) {
                        return AmbiguousType.IntOrFloatOrRef.getInstance();
                    }
                } else if (typeA instanceof AmbiguousType.IntOrFloat || typeB instanceof AmbiguousType.IntOrFloat) {
                    if (isIntOrFloatType(typeA) && isIntOrFloatType(typeB)) {
                        return AmbiguousType.IntOrFloat.getInstance();
                    }
                } else if (typeA instanceof AmbiguousType.LongOrDouble || typeB instanceof AmbiguousType.LongOrDouble) {
                    if (isLongOrDoubleType(typeA) && isLongOrDoubleType(typeB)) {
                        return AmbiguousType.LongOrDouble.getInstance();
                    }
                } else {
                    return DexUtils.unreachable();
                }
            } else {
                if (typeA instanceof IntLikeType && typeB instanceof IntLikeType) {
                    return IntType.getInstance();
                } else if (typeA instanceof RefType && typeB instanceof RefType) {
                    return ObjectType.OBJECT;
                } else if ((typeA instanceof IntType || typeA instanceof FloatType || typeA instanceof RefType) &&
                        (typeB instanceof IntType || typeB instanceof FloatType || typeB instanceof RefType)) {
                    if (typeA instanceof RefType || typeB instanceof RefType) {
                        return AmbiguousType.IntOrFloatOrRef.getInstance();
                    } else {
                        return AmbiguousType.IntOrFloat.getInstance();
                    }
                } else if ((typeA instanceof LongType || typeA instanceof DoubleType) &&
                        (typeB instanceof LongType || typeA instanceof DoubleType)) {
                    return AmbiguousType.LongOrDouble.getInstance();
                }
            }

            throw new IllegalStateException("Cannot merge types " + typeA + " and " + typeB);
        }

        /**
         * Merge two types into one common type.
         *
         * If one type is less ambiguous / more precise than the other type,
         * than that type dominates and is returned.
         *
         * @param typeA one type
         * @param typeB another type
         * @return the merged type or a {@link TypeConflict}
         */
        public Type mergePreciseDominating(Type typeA, Type typeB) {
            if (typeA == null) return typeB;
            if (typeB == null) return typeA;
            if (typeA.equals(typeB)) return typeA;

            if (typeA instanceof TypeConflict || typeB instanceof TypeConflict) {
                return TypeConflict.INSTANCE;
            }

            if (typeA instanceof AmbiguousType) {
                return mergeAmbiguousTypePreciseDominating((AmbiguousType) typeA, typeB);
            }
            if (typeB instanceof AmbiguousType) {
                return mergeAmbiguousTypePreciseDominating((AmbiguousType) typeB, typeA);
            }

            if (typeA instanceof IntLikeType && typeB instanceof IntLikeType) {
                return IntType.getInstance();
            } else if (typeA instanceof RefType && typeB instanceof RefType) {
                if (typeA instanceof ArrayType) return typeA;
                if (typeB instanceof ArrayType) return typeB;
                return ObjectType.OBJECT;
            } else {
                return TypeConflict.INSTANCE;
            }
        }

        /**
         * Merge an ambiguous Type with another type.
         *
         * If one type is more precise / less ambiguous than the other type,
         * than that type dominates and is returned.
         *
         * @param ambiguousType an ambiguous type
         * @param type an ambiguous or non-ambiguous type to be merged in
         * @return the more precise type of both or a {@link TypeConflict}
         */
        private Type mergeAmbiguousTypePreciseDominating(AmbiguousType ambiguousType, Type type) {
            if (ambiguousType instanceof AmbiguousType.LongOrDouble) {
                if (isLongOrDoubleType(type)) {
                    return type;
                }
            } else if (ambiguousType instanceof AmbiguousType.IntOrFloat) {
                if (type instanceof AmbiguousType.IntOrFloatOrRef) {
                    // Since IntOrFloat is a more precise than IntOrFloatOrRef, we return it
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

            return TypeConflict.INSTANCE;
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

        /**
         * Merge two types that are both non-ambiguous.
         *
         * @param typeA a non-ambiguous type
         * @param typeB another non-ambiguous type
         * @return the merged type
         */
        public Type mergePreciseTypes(Type typeA, Type typeB) {
            if (typeA instanceof AmbiguousType || typeB instanceof AmbiguousType ||
                    typeA instanceof TypeConflict || typeB instanceof TypeConflict) {
                throw new IllegalArgumentException("Precise types are required, got " + typeA + " and " + typeB);
            }

            if (typeA.equals(typeB)) {
                return typeA;
            } else if (typeA instanceof IntLikeType && typeB instanceof IntLikeType) {
                return IntType.getInstance();
            } else if (typeA instanceof RefType && typeB instanceof RefType) {
                return ObjectType.OBJECT;
            }

            throw new IllegalArgumentException("Cannot merge types " + typeA + " and " + typeB);
        }
    }
}
