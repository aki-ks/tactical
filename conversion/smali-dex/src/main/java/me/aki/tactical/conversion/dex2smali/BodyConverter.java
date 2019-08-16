package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.conversion.dex2smali.provider.InstructionProvider;
import me.aki.tactical.conversion.dex2smali.provider.OffsetCell;
import me.aki.tactical.conversion.smali2dex.InstructionIndex;
import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.LinkedInsertList;
import me.aki.tactical.core.utils.AbstractCfgGraph;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.TryCatchBlock;
import me.aki.tactical.dex.insn.Instruction;
import me.aki.tactical.dex.utils.DexCfgGraph;
import me.aki.tactical.dex.utils.DexInsnReader;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.immutable.ImmutableExceptionHandler;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableTryBlock;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BodyConverter {
    private final Method method;
    private final DexBody body;
    private final DexCfgGraph cfgGraph;

    public BodyConverter(Method method, DexBody body) {
        this.method = method;
        this.body = body;
        this.cfgGraph = new DexCfgGraph(body);
    }

    public ImmutableMethodImplementation convert() {
        InstructionConverter insnConverter = new InstructionConverter();
        Instructions insns = new Instructions(insnConverter);

        List<TryBlock<? extends ExceptionHandler>> tryBlocks = convertTryCatchBlocks(insnConverter, insns);
        List<? extends DebugItem> debugItems = convertDebugItems();

        return new ImmutableMethodImplementation(insnConverter.getRegisterCount(), insns.getInstructions(), tryBlocks, debugItems);
    }

    private List<TryBlock<? extends ExceptionHandler>> convertTryCatchBlocks(InstructionConverter converter, Instructions insns) {
        return this.body.getTryCatchBlocks().stream()
                .filter(block -> containsNonDeadCode(converter, block))
                .map(tryCatchBlock -> {
                    int start = insns.getFirstSucceedingNonDead(tryCatchBlock.getStart());
                    int end = insns.getFirstPrecedingNonDead(tryCatchBlock.getEnd());

                    List<ImmutableExceptionHandler> handlers = tryCatchBlock.getHandlers().stream().map(handler -> {
                        String exception = handler.getException().map(DexUtils::toObjectDescriptor).orElse(null);
                        int offset = insns.getFirstSucceedingNonDead(handler.getHandler());
                        return new ImmutableExceptionHandler(exception, offset);
                    }).collect(Collectors.toList());

                    return new ImmutableTryBlock(start, end, handlers);
                }).collect(Collectors.toList());
    }

    /**
     * Check whether any of the instructions covered by this try/catch block is not dead code
     * and has therefore been converted to smali representation.
     *
     * @param tryCatchBlock a try catch block
     * @return does the try/catch block cover any non-dead instruction
     */
    private boolean containsNonDeadCode(InstructionConverter converter, TryCatchBlock tryCatchBlock) {
        Iterator<Instruction> iterator = this.body.getInstructions().iterator(tryCatchBlock.getStart(), tryCatchBlock.getEnd());
        while (iterator.hasNext()) {
            if (!converter.isDeadCode(iterator.next())) {
                return true;
            }
        }
        return false;
    }

    private List<? extends DebugItem> convertDebugItems() {
        //TODO Implement dex -> smali conversion of debug items
        return new ArrayList<>();
    }

    /**
     * The first step of the conversion process where we first create {@link InstructionProvider InstructionProviders}
     * for each tactical instruction and then resolve the register indices and all branch offsets.
     */
    private class InstructionConverter {
        private final Map<Instruction, InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction>> convertedInsns;
        private final List<RegisterConstraint> registerConstraints;
        private final InsertList<InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction>> instructionProviders;
        private final int registerCount;

        public InstructionConverter() {
            SmaliInsnWriter writer = new SmaliInsnWriter(method.getReturnType());
            DexInsnReader reader = new DexInsnReader(writer);

            this.convertedInsns = convertInstructions(writer, reader);
            this.registerConstraints = writer.getRegisterConstraints();

            this.instructionProviders = body.getInstructions().stream()
                    .map(this.convertedInsns::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedInsertList::new));
            this.instructionProviders.addAll(writer.getPayloadInstructions());

            // The targets of some OffsetCells have temporary been assigned to tactical Instructions.
            // These can now be replaced against the corresponding InstructionProvider.
            resolveOffsetCells();

            this.registerCount = fillRegisterCells();

            fillOffsetCells();
        }

        private Map<Instruction, InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction>> convertInstructions(SmaliInsnWriter writer, DexInsnReader reader) {
            Map<Instruction, InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction>> convertedInsns = new HashMap<>();

            cfgGraph.forEachNode(node -> {
                writer.setInstruction(node);
                reader.accept(node.getInstruction());

                InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction> insnProvider = getConvertedInstruction(writer, node);
                convertedInsns.put(node.getInstruction(), insnProvider);
            });

            return convertedInsns;
        }

        private InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction> getConvertedInstruction(SmaliInsnWriter writer, AbstractCfgGraph<Instruction>.Node node) {
            List<InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction>> insns = writer.getInstructions();
            switch (insns.size()) {
                case 1:
                    InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction> insn = insns.get(0);
                    insns.clear();
                    return insn;

                case 0: throw new IllegalStateException("Conversion of " + node.getInstruction().getClass().getSimpleName() + " emitted no smali instruction");
                default: throw new IllegalStateException("Conversion of " + node.getInstruction().getClass().getSimpleName() + " emitted multiple smali instructions");
            }
        }

        private void resolveOffsetCells() {
            Stream<OffsetCell> unresolvedOffsetCells = this.instructionProviders.stream()
                    .flatMap(insn -> insn.getOffsetCells().stream())
                    .filter(OffsetCell::isUnresolved);

            unresolvedOffsetCells.forEach(offsetCell -> {
                InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction> resolvedTarget =
                        this.convertedInsns.get(offsetCell.getUnresolvedTarget());
                offsetCell.resolveTarget(resolvedTarget);
            });
        }

        /**
         * Fill the RegisterCells of all {@link InstructionConverter#instructionProviders} and return the amount of total registers.
         *
         * @return register count of the method
         */
        private int fillRegisterCells() {
            RegisterConstraintSolver solver = new RegisterConstraintSolver(body, this.instructionProviders, this.registerConstraints);
            Map<Register, Integer> registerMap = new RegisterIndexAssigner(body, solver).getRegisterMap();

            this.instructionProviders.stream()
                    .flatMap(provider -> provider.getRegisterCells().stream())
                    .forEach(cell -> cell.set(registerMap.get(cell.getRegister())));

            return registerMap.values().stream().max(Integer::compare)
                    .map(maxIndex -> maxIndex + 1).orElse(0);
        }

        private void fillOffsetCells() {
            new CodeUnitComputation(this.instructionProviders).updateOffsets();
        }

        public InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction> getInsnProvider(Instruction instruction) {
            return this.convertedInsns.get(instruction);
        }

        public boolean isDeadCode(Instruction instruction) {
            return !this.convertedInsns.containsKey(instruction);
        }

        public int getRegisterCount() {
            return this.registerCount;
        }
    }

    /**
     * Create all instruction instances based on the work of the {@link InstructionConverter}.
     */
    private class Instructions {
        private final Map<InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction>, org.jf.dexlib2.iface.instruction.Instruction> insnInstances;
        private final InstructionIndex instructionIndex;
        private final InstructionConverter converter;

        public Instructions(InstructionConverter converter) {
            this.converter = converter;
            this.insnInstances = new HashMap<>();

            List<org.jf.dexlib2.iface.instruction.Instruction> instructions =
                    converter.instructionProviders.stream().map(provider -> {
                        org.jf.dexlib2.iface.instruction.Instruction smaliInsn = provider.newInstance();
                        this.insnInstances.put(provider, smaliInsn);
                        return smaliInsn;
                    }).collect(Collectors.toList());

            this.instructionIndex = new InstructionIndex(instructions);
        }

        public InsertList<org.jf.dexlib2.iface.instruction.Instruction> getInstructions() {
            return this.instructionIndex.getInstructions();
        }

        private int getFirstSucceedingNonDead(Instruction instruction) {
            while (true) {
                InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction> provider = converter.getInsnProvider(instruction);
                if (provider != null) {
                    org.jf.dexlib2.iface.instruction.Instruction smaliInsn = this.insnInstances.get(provider);
                    return this.instructionIndex.getCodeUnit(smaliInsn);
                }
                instruction = body.getInstructions().getNext(instruction);
            }
        }

        private int getFirstPrecedingNonDead(Instruction instruction) {
            while (true) {
                InstructionProvider<? extends org.jf.dexlib2.iface.instruction.Instruction> provider = converter.getInsnProvider(instruction);
                if (provider != null) {
                    org.jf.dexlib2.iface.instruction.Instruction smaliInsn = this.insnInstances.get(provider);
                    return this.instructionIndex.getCodeUnit(smaliInsn);
                }
                instruction = body.getInstructions().getPrevious(instruction);
            }
        }
    }
}
