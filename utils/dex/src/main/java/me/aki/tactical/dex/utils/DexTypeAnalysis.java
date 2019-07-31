package me.aki.tactical.dex.utils;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.core.constant.DexNumber32Constant;
import me.aki.tactical.core.constant.DexNumber64Constant;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.type.*;
import me.aki.tactical.dex.DetailedDexType;
import me.aki.tactical.dex.DexType;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.insn.FillArrayInstruction;
import me.aki.tactical.dex.insn.IfInstruction;
import me.aki.tactical.dex.insn.Instruction;

import java.util.*;
import java.util.function.Consumer;

public class DexTypeAnalysis {
    private final DexCfgGraph cfgGraph;
    private final Map<Instruction, RegisterState> states = new HashMap<>();

    public DexTypeAnalysis(DexCfgGraph cfgGraph) {
        this.cfgGraph = cfgGraph;

        this.computeWriteTypes();
        this.computeRegisterStates();

        this.validateRegisterStates();
    }

    public RegisterState getRegisterStates(Instruction location) {
        return this.states.get(location);
    }

    /**
     * Store for each instruction which register it writes to and what type the value will be of.
     */
    private void computeWriteTypes() {
        forEachNode(node -> {
            Instruction instruction = node.getInstruction();

            RegisterState state = new RegisterState(instruction);
            this.states.put(instruction, state);

            new DexInsnReader(new TypeWriteVisitor() {
                @Override
                protected void visitTypeWrite(Register register, DexType type) {
                    state.setWrittenRegister(register, type);
                }
            }).accept(node.getInstruction());
        });
    }

    private void forEachNode(Consumer<DexCfgGraph.Node> function) {
        Set<DexCfgGraph.Node> visitedNodes = new HashSet<>();
        Deque<DexCfgGraph.Node> worklist = new ArrayDeque<>();

        worklist.add(cfgGraph.getHead());
        worklist.addAll(cfgGraph.getHandlerNodes());

        while (!worklist.isEmpty()) {
            DexCfgGraph.Node node = worklist.poll();
            if (!visitedNodes.add(node)) {
                // This instruction was already visited
                continue;
            }

            function.accept(node);

            worklist.addAll(node.getSucceeding());
        }
    }

    /**
     * Compute for each instruction which types the registers will have before the instruction was executed.
     */
    private void computeRegisterStates() {
        forEachNode(node -> {
            RegisterState state = this.states.get(node.getInstruction());
            if (state.writtenRegister != null) {
                for (DexCfgGraph.Node successor : node.getSucceeding()) {
                    propagateTypeForward(successor, state.writtenRegister, state.writtenType);
                }
            }
        });
    }

    /**
     * Store for all succeeding instruction that a register will have a certain type
     * until we pass an instruction that writes another type.
     *
     * @param node the register has the type from here on.
     * @param register the register
     * @param type the type that the register will have.
     */
    private void propagateTypeForward(DexCfgGraph.Node node, Register register, DexType type) {
        Instruction instruction = node.getInstruction();
        RegisterState state = this.states.get(instruction);

        if (state.addRegisterTypeBefore(register, type)) {
            if (state.writtenRegister != register) {
                for (DexCfgGraph.Node successor : node.getSucceeding()) {
                    propagateTypeForward(successor, register, type);
                }
            }
        }
    }

    /**
     * The state of the registers before and after one instruction.
     */
    public class RegisterState {
        private final Instruction instruction;

        /**
         * The register that this instruction writes to, if any
         */
        private Register writtenRegister = null;
        /**
         * What type will the written value have, if any
         */
        private DexType writtenType = null;

        /**
         * Map registers to the types that they may possible have before this instruction was executed.
         */
        private final Map<Register, Set<DexType>> statesBefore = new HashMap<>();

        public RegisterState(Instruction instruction) {
            this.instruction = instruction;
        }

        private void setWrittenRegister(Register register, DexType type) {
            if (this.writtenRegister != null) {
                // Would only happen if a dex instruction writes to multiple registers
                throw new IllegalStateException();
            }

            this.writtenRegister = register;
            this.writtenType = type;
        }

        private boolean addRegisterTypeBefore(Register register, DexType type) {
            Set<DexType> types = this.statesBefore.computeIfAbsent(register, x -> new HashSet<>());
            return types.add(type);
        }

        /**
         * Does the instruction write to any register
         * @param register
         * @return
         */
        public boolean writesToRegister(Register register) {
            return writtenRegister != null;
        }

        /**
         * Get the register that the instruction writes to.
         *
         * @return the written register
         */
        public Optional<Register> getWrittenToRegister() {
            return Optional.ofNullable(writtenRegister);
        }

        /**
         * Get the possible types that a register may have before this instruction was executed.
         *
         * @param register the register
         * @return types that the register may have
         */
        public Set<DexType> getTypesBefore(Register register) {
            return Collections.unmodifiableSet(statesBefore.getOrDefault(register, Set.of()));
        }

        /**
         * Get the possible types that a register may have after this instruction was executed.
         *
         * @param register the register
         * @return types that the register may have
         */
        public Set<DexType> getTypesAfter(Register register) {
            if (register == writtenRegister) {
                return Set.of(writtenType);
            } else {
                return Collections.unmodifiableSet(getTypesBefore(register));
            }
        }
    }

    /**
     * Check whether the type that the instructions requires in registers match those that previous instructions wrote.
     */
    private void validateRegisterStates() {
        forEachNode(node -> {
            Instruction instruction = node.getInstruction();
            RegisterState state = getRegisterStates(instruction);
            new DexInsnReader(new TypeReadVisitor() {
                @Override
                protected void visitTypeRead(Register register, DexType requiredType) {
                    Set<DexType> typesBefore = state.getTypesBefore(register);
                    switch (typesBefore.size()) {
                        case 0: throw new IllegalStateException("Register was never written before read");
                        default: throw new IllegalStateException("Register has no distinct type: " + typesBefore);

                        case 1:
                            DexType type = typesBefore.iterator().next();
                            if (type == requiredType) {
                                break;
                            } else {
                                throw new IllegalStateException("Instruction requires type " + requiredType + " in register, but type " + type + " is actually present");
                            }
                    }
                }
            }).accept(instruction);
        });
    }

    private static abstract class AbstractTypeVisitor extends DexInsnVisitor<Instruction, Register> {
        protected DexType typeOf(Type type) {
            if (type instanceof PrimitiveType) {
                if (type instanceof LongType || type instanceof DoubleType) {
                    return DexType.WIDE;
                } else {
                    return DexType.NORMAL;
                }
            } else if (type instanceof RefType) {
                return DexType.OBJECT;
            } else {
                throw new AssertionError();
            }
        }

        protected DexType typeOf(DetailedDexType type) {
            return type.toDexType();
        }

        @Override
        public void visitAdd(PrimitiveType type, Register op1, Register op2, Register result) {
            visitMathInsn(type, op1, op2, result);
        }

        @Override
        public void visitSub(PrimitiveType type, Register op1, Register op2, Register result) {
            visitMathInsn(type, op1, op2, result);
        }

        @Override
        public void visitMul(PrimitiveType type, Register op1, Register op2, Register result) {
            visitMathInsn(type, op1, op2, result);
        }

        @Override
        public void visitDiv(PrimitiveType type, Register op1, Register op2, Register result) {
            visitMathInsn(type, op1, op2, result);
        }

        @Override
        public void visitMod(PrimitiveType type, Register op1, Register op2, Register result) {
            visitMathInsn(type, op1, op2, result);
        }

        @Override
        public void visitAnd(PrimitiveType type, Register op1, Register op2, Register result) {
            visitMathInsn(type, op1, op2, result);
        }

        @Override
        public void visitOr(PrimitiveType type, Register op1, Register op2, Register result) {
            visitMathInsn(type, op1, op2, result);
        }

        @Override
        public void visitXor(PrimitiveType type, Register op1, Register op2, Register result) {
            visitMathInsn(type, op1, op2, result);
        }

        @Override
        public void visitShl(PrimitiveType type, Register op1, Register op2, Register result) {
            visitBitShiftInsn(type, op1, op2, result);
        }

        @Override
        public void visitShr(PrimitiveType type, Register op1, Register op2, Register result) {
            visitBitShiftInsn(type, op1, op2, result);
        }

        @Override
        public void visitUShr(PrimitiveType type, Register op1, Register op2, Register result) {
            visitBitShiftInsn(type, op1, op2, result);
        }

        @Override
        public void visitLitAdd(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitRSub(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitMul(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitDiv(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitMod(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitAnd(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitOr(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitXor(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitShl(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitShr(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        @Override
        public void visitLitUShr(Register op1, short literal, Register result) {
            visitLitMath(op1, result);
        }

        protected abstract void visitMathInsn(PrimitiveType type, Register op1, Register op2, Register result);
        protected abstract void visitBitShiftInsn(PrimitiveType type, Register op1, Register op2, Register result);
        protected abstract void visitLitMath(Register op1, Register result);
    }

    /**
     * Visit for each instruction which registers it reads from and which types it expects
     */
    private static abstract class TypeReadVisitor extends AbstractTypeVisitor {
        protected abstract void visitTypeRead(Register register, DexType type);

        @Override
        public void visitConstant(DexConstant constant, Register target) {}

        @Override
        protected void visitMathInsn(PrimitiveType type, Register op1, Register op2, Register result) {
            DexType dexType = typeOf(type);
            visitTypeRead(op1, dexType);
            visitTypeRead(op2, dexType);
        }

        @Override
        protected void visitBitShiftInsn(PrimitiveType type, Register op1, Register op2, Register result) {
            DexType dexType = typeOf(type);
            visitTypeRead(op1, dexType);
            visitTypeRead(op2, typeOf(IntType.getInstance()));
        }

        @Override
        protected void visitLitMath(Register op1, Register result) {
            visitTypeRead(op1, typeOf(IntType.getInstance()));
        }

        @Override
        public void visitNeg(PrimitiveType type, Register value, Register result) {
            visitTypeRead(value, typeOf(type));
        }

        @Override
        public void visitNot(PrimitiveType type, Register value, Register result) {
            visitTypeRead(value, typeOf(type));
        }

        @Override
        public void visitCmp(Register op1, Register op2, Register result) {
            visitTypeRead(op1, typeOf(LongType.getInstance()));
            visitTypeRead(op2, typeOf(LongType.getInstance()));
        }

        @Override
        public void visitCmpl(PrimitiveType type, Register op1, Register op2, Register result) {
            visitTypeRead(op1, typeOf(type));
            visitTypeRead(op2, typeOf(type));
        }

        @Override
        public void visitCmpg(PrimitiveType type, Register op1, Register op2, Register result) {
            visitTypeRead(op1, typeOf(type));
            visitTypeRead(op2, typeOf(type));
        }

        @Override
        public void visitArrayLength(Register array, Register result) {
            visitTypeRead(array, DexType.OBJECT);
        }

        @Override
        public void visitArrayLoad(DetailedDexType type, Register array, Register index, Register result) {
            visitTypeRead(array, DexType.OBJECT);
            visitTypeRead(index, typeOf(IntType.getInstance()));
        }

        @Override
        public void visitArrayStore(DetailedDexType type, Register array, Register index, Register value) {
            visitTypeRead(array, DexType.OBJECT);
            visitTypeRead(index, typeOf(IntType.getInstance()));
            visitTypeRead(value, typeOf(type));
        }

        @Override
        public void visitFillArray(Register array, FillArrayInstruction.NumberSize elementSize, List<FillArrayInstruction.NumericConstant> values) {
            visitTypeRead(array, DexType.OBJECT);
        }

        @Override
        public void visitNewArray(ArrayType type, Register size, Register result) {}

        @Override
        public void visitNewFilledArray(ArrayType type, List<Register> registers) {
            DexType elementType = typeOf(type.getLowerType());
            for (Register register : registers) {
                visitTypeRead(register, elementType);
            }
        }

        @Override
        public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, Register fromRegister, Register toRegister) {
            visitTypeRead(fromRegister, typeOf(fromType));
        }

        @Override
        public void visitRefCast(RefType type, Register register) {
            visitTypeRead(register, DexType.OBJECT);
        }

        @Override
        public void visitMonitorEnter(Register value) {
            visitTypeRead(value, DexType.OBJECT);
        }

        @Override
        public void visitMonitorExit(Register value) {
            visitTypeRead(value, DexType.OBJECT);
        }

        @Override
        public void visitNew(Path type, Register result) {}

        @Override
        public void visitInstanceOf(RefType type, Register value, Register result) {
            visitTypeRead(value, typeOf(type));
        }

        @Override
        public void visitReturn(DexType type, Register register) {
            visitTypeRead(register, type);
        }

        @Override
        public void visitReturnVoid() {}

        @Override
        public void visitThrow(Register exception) {
            visitTypeRead(exception, DexType.OBJECT);
        }

        @Override
        public void visitFieldGet(FieldRef field, Optional<Register> instanceOpt, Register result) {
            instanceOpt.ifPresent(instance -> visitTypeRead(instance, DexType.OBJECT));
        }

        @Override
        public void visitFieldSet(FieldRef field, Optional<Register> instanceOpt, Register value) {
            instanceOpt.ifPresent(instance -> visitTypeRead(instance, DexType.OBJECT));
            visitTypeRead(value, typeOf(field.getType()));
        }

        @Override
        public void visitInvoke(InvokeType invoke, MethodRef method, Optional<Register> instanceOpt, List<Register> arguments) {
            instanceOpt.ifPresent(instance -> visitTypeRead(instance, DexType.OBJECT));

            Iterator<Type> typeIterator = method.getArguments().iterator();
            for (Register argument : arguments) {
                visitTypeRead(argument, typeOf(typeIterator.next()));
            }
        }

        @Override
        public void visitPolymorphicInvoke(MethodRef method, MethodDescriptor descriptor, Register instance, List<Register> arguments) {
            visitTypeRead(instance, DexType.OBJECT);

            Iterator<Type> typeIterator = method.getArguments().iterator();
            for (Register argument : arguments) {
                visitTypeRead(argument, typeOf(typeIterator.next()));
            }
        }

        @Override
        public void visitCustomInvoke(List<Register> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
            Iterator<Type> typeIterator = descriptor.getParameterTypes().iterator();
            for (Register argument : arguments) {
                visitTypeRead(argument, typeOf(typeIterator.next()));
            }
        }

        @Override
        public void visitMove(DexType type, Register from, Register to) {
            visitTypeRead(from, type);
        }

        @Override
        public void visitMoveResult(DexType type, Register register) {}

        @Override
        public void visitMoveException(Register target) {}

        @Override
        public void visitGoto(Instruction target) {}

        @Override
        public void visitIf(IfInstruction.Comparison comparison, Register op1, Optional<Register> op2Opt, Instruction target) {
            visitTypeRead(op1, typeOf(IntType.getInstance()));
            op2Opt.ifPresent(op2 -> visitTypeRead(op2, typeOf(IntType.getInstance())));
        }

        @Override
        public void visitSwitch(Register value, LinkedHashMap<Integer, Instruction> branchTable) {
            visitTypeRead(value, typeOf(IntType.getInstance()));
        }
    }

    /**
     * Visit for each instruction which registers it writes to and which type the written value has
     */
    private static abstract class TypeWriteVisitor extends AbstractTypeVisitor {
        protected abstract void visitTypeWrite(Register register, DexType type);

        @Override
        public void visitConstant(DexConstant constant, Register target) {
            if (constant instanceof DexNumber32Constant) {
                visitTypeWrite(target, DexType.NORMAL);
            } else if (constant instanceof DexNumber64Constant) {
                visitTypeWrite(target, DexType.WIDE);
            } else {
                visitTypeWrite(target, DexType.OBJECT);
            }
        }

        @Override
        protected void visitMathInsn(PrimitiveType type, Register op1, Register op2, Register result) {
            DexType dexType = typeOf(type);
            visitTypeWrite(result, dexType);
        }

        @Override
        protected void visitBitShiftInsn(PrimitiveType type, Register op1, Register op2, Register result) {
            DexType dexType = typeOf(type);
            visitTypeWrite(result, dexType);
        }

        @Override
        protected void visitLitMath(Register op1, Register result) {
            visitTypeWrite(result, typeOf(IntType.getInstance()));
        }

        @Override
        public void visitNeg(PrimitiveType type, Register value, Register result) {
            visitTypeWrite(result, typeOf(type));
        }

        @Override
        public void visitNot(PrimitiveType type, Register value, Register result) {
            visitTypeWrite(result, typeOf(type));
        }

        @Override
        public void visitCmp(Register op1, Register op2, Register result) {
            visitTypeWrite(result, typeOf(IntType.getInstance()));
        }

        @Override
        public void visitCmpl(PrimitiveType type, Register op1, Register op2, Register result) {
            visitTypeWrite(result, typeOf(IntType.getInstance()));
        }

        @Override
        public void visitCmpg(PrimitiveType type, Register op1, Register op2, Register result) {
            visitTypeWrite(result, typeOf(IntType.getInstance()));
        }

        @Override
        public void visitArrayLength(Register array, Register result) {
            visitTypeWrite(result, typeOf(IntType.getInstance()));
        }

        @Override
        public void visitArrayLoad(DetailedDexType type, Register array, Register index, Register result) {
            visitTypeWrite(result, typeOf(type));
        }

        @Override
        public void visitArrayStore(DetailedDexType type, Register array, Register index, Register value) {}

        @Override
        public void visitFillArray(Register array, FillArrayInstruction.NumberSize elementSize, List<FillArrayInstruction.NumericConstant> values) {}

        @Override
        public void visitNewArray(ArrayType type, Register size, Register result) {
            visitTypeWrite(result, typeOf(type));
        }

        @Override
        public void visitNewFilledArray(ArrayType type, List<Register> registers) {}

        @Override
        public void visitPrimitiveCast(PrimitiveType fromType, PrimitiveType toType, Register fromRegister, Register toRegister) {
            visitTypeWrite(toRegister, typeOf(toType));
        }

        @Override
        public void visitRefCast(RefType type, Register register) {}

        @Override
        public void visitMonitorEnter(Register value) {}

        @Override
        public void visitMonitorExit(Register value) {}

        @Override
        public void visitNew(Path type, Register result) {
            visitTypeWrite(result, DexType.OBJECT);
        }

        @Override
        public void visitInstanceOf(RefType type, Register value, Register result) {
            visitTypeWrite(result, typeOf(IntType.getInstance()));
        }

        @Override
        public void visitReturn(DexType type, Register register) {}

        @Override
        public void visitReturnVoid() {}

        @Override
        public void visitThrow(Register exception) {}

        @Override
        public void visitFieldGet(FieldRef field, Optional<Register> instanceOpt, Register result) {
            visitTypeWrite(result, typeOf(field.getType()));
        }

        @Override
        public void visitFieldSet(FieldRef field, Optional<Register> instanceOpt, Register value) {}

        @Override
        public void visitInvoke(InvokeType invoke, MethodRef method, Optional<Register> instanceOpt, List<Register> arguments) {}

        @Override
        public void visitPolymorphicInvoke(MethodRef method, MethodDescriptor descriptor, Register instance, List<Register> arguments) {}

        @Override
        public void visitCustomInvoke(List<Register> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {}

        @Override
        public void visitMove(DexType type, Register from, Register to) {
            visitTypeWrite(to, type);
        }

        @Override
        public void visitMoveResult(DexType type, Register register) {
            visitTypeWrite(register, type);
        }

        @Override
        public void visitMoveException(Register target) {
            visitTypeWrite(target, DexType.OBJECT);
        }

        @Override
        public void visitGoto(Instruction target) {}

        @Override
        public void visitIf(IfInstruction.Comparison comparison, Register op1, Optional<Register> op2Opt, Instruction target) {}

        @Override
        public void visitSwitch(Register value, LinkedHashMap<Integer, Instruction> branchTable) {}
    }
}
