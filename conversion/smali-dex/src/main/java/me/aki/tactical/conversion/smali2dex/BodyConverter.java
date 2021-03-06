package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.conversion.smali2dex.typing.RegisterPartitioner;
import me.aki.tactical.conversion.smali2dex.typing.DexTyper;
import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.TryCatchBlock;
import me.aki.tactical.dex.utils.DexCfgGraph;
import org.jf.dexlib2.DebugItemType;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.debug.*;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for conversion of a smali {@link MethodImplementation} body into a tactical {@link DexBody}
 */
public class BodyConverter {
    private final Method method;
    private final MethodImplementation smaliBody;

    private final DexBody body;
    private final InstructionIndex insnIndex;
    private final SmaliCfgGraph smaliCfg;

    /**
     * Map smali instructions to their converted tactical dex form.
     */
    private final Map<Instruction, me.aki.tactical.dex.insn.Instruction> convertedInsns = new HashMap<>();

    /**
     * Converted non-deadcode instructions that translated into no tactical instruction.
     * These are for instance payload instructions.
     */
    private final Set<Instruction> nonConvertedInstructions = new HashSet<>();

    /**
     * Map instruction referencing cells to the smali representation of the instruction that they should point to.
     * Once all smali instructions have been converted, the cells get set.
     *
     * @see BodyConverter#updateInsnReferences()
     */
    private final Map<Instruction, Collection<RWCell<me.aki.tactical.dex.insn.Instruction>>> insnRefs = new HashMap<>();

    public BodyConverter(Method method, MethodImplementation smaliBody) {
        this.method = method;
        this.smaliBody = smaliBody;

        this.body = new DexBody();
        this.insnIndex = new InstructionIndex(smaliBody.getInstructions());
        this.smaliCfg = new SmaliCfgGraph(this.insnIndex, smaliBody.getTryBlocks());

        this.doConversion();
        this.doTyping();
    }

    private void doConversion() {
        this.convertRegisters();

        this.convertInsns();
        this.updateInsnReferences();

        this.convertTryCatchBlocks();

        this.convertDebug();
    }

    public DexBody getBody() {
        return body;
    }

    private void convertRegisters() {
        final int registerCount = smaliBody.getRegisterCount();
        List<Register> registers = new ArrayList<>(registerCount);
        for (int i = 0; i < registerCount; i++) {
            registers.add(new Register(null));
        }

        int firstParameterIndex = convertParameterRegisters(registers, registerCount);

        if (!method.getFlag(Method.Flag.STATIC)) {
            Register thisRegister = registers.get(firstParameterIndex - 1);
            body.setThisRegister(Optional.of(thisRegister));
        }

        body.setRegisters(registers);
    }

    private int convertParameterRegisters(List<Register> registers, int cursor) {
        LinkedList<Register> parameterRegisters = new LinkedList<>(); // O(1) insertion at index 0

        ListIterator<Type> parameterIterator = method.getParameterTypes().listIterator(method.getParameterTypes().size());
        while (parameterIterator.hasPrevious()) {
            Type paramType = parameterIterator.previous();
            int registerSize = paramType instanceof LongType || paramType instanceof DoubleType ? 2 : 1;
            cursor -= registerSize;

            Register parameterRegister = registers.get(cursor);
            parameterRegisters.add(0, parameterRegister);
        }

        body.setParameterRegisters(new ArrayList<>(parameterRegisters));
        return cursor;
    }

    private void convertInsns() {
        SmaliDexInsnWriter writer = new SmaliDexInsnWriter(body.getRegisters(), insnRefs);
        SmaliDexInsnReader reader = new SmaliDexInsnReader(insnIndex, writer);

        smaliCfg.forEachNode(node -> {
            reader.accept(node.getInstruction());

            List<me.aki.tactical.dex.insn.Instruction> instructions = writer.popInstructions();
            switch (instructions.size()) {
                case 0:
                    this.nonConvertedInstructions.add(node.getInstruction());
                    break;

                case 1:
                    this.convertedInsns.put(node.getInstruction(), instructions.get(0));
                    break;

                default:
                    throw new IllegalStateException("One smali instruction translated into multiple tactical dex instructions");
            }
        });

        for (Instruction smaliInsn : insnIndex.getInstructions()) {
            me.aki.tactical.dex.insn.Instruction insn = this.convertedInsns.get(smaliInsn);
            if (insn != null) body.getInstructions().add(insn);
        }
    }

    private void convertTryCatchBlocks() {
        for (TryBlock<? extends ExceptionHandler> smaliBlock : smaliBody.getTryBlocks()) {
            Instruction smaliStart = insnIndex.getInstructionByCodeUnit(smaliBlock.getStartCodeAddress());
            Instruction smaliEnd = insnIndex.getInstructionByCodeUnit(smaliBlock.getStartCodeAddress() + smaliBlock.getCodeUnitCount());

            firstNonDeadInsnInRange(smaliStart, smaliEnd).ifPresent(start -> {
                me.aki.tactical.dex.insn.Instruction end = lastNonDeadInsnInRange(smaliStart, smaliEnd).get();

                List<TryCatchBlock.Handler> handlers = smaliBlock.getExceptionHandlers().stream()
                        .map(this::convertExceptionHandler).filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (handlers.size() > 0) {
                    body.getTryCatchBlocks().add(new TryCatchBlock(start, end, handlers));
                }
            });
        }
    }

    private TryCatchBlock.Handler convertExceptionHandler(ExceptionHandler smaliHandler) {
        Instruction smaliHandlerInsn = insnIndex.getInstructionByCodeUnit(smaliHandler.getHandlerCodeAddress());
        me.aki.tactical.dex.insn.Instruction handlerInsn = convertedInstructionOf(smaliHandlerInsn);
        if (handlerInsn == null) return null;

        Optional<Path> exception = Optional.ofNullable(smaliHandler.getExceptionType())
                .map(DexUtils::parseObjectDescriptor);

        return new TryCatchBlock.Handler(exception, handlerInsn);
    }

    private void updateInsnReferences() {
        this.insnRefs.forEach((smaliInsn, refs) -> {
            me.aki.tactical.dex.insn.Instruction insn = convertedInstructionOf(smaliInsn);
            for (RWCell<me.aki.tactical.dex.insn.Instruction> cell : refs) {
                cell.set(insn);
            }
        });
    }

    /**
     * Find the first instruction within a range of instructions that has been converted.
     *
     * @param start first instruction of the range
     * @param end last instruction of the range (exclusive)
     * @return the found instruction or {@link Optional#empty()} if the range contains only dead code
     */
    private Optional<me.aki.tactical.dex.insn.Instruction> firstNonDeadInsnInRange(Instruction start, Instruction end) {
        InsertList<Instruction> insns = this.insnIndex.getInstructions();
        for (Instruction insn = start; insn != null; insn = insns.getNext(insn)) {
            me.aki.tactical.dex.insn.Instruction tacticalInsn = this.convertedInsns.get(insn);
            if (tacticalInsn != null) return Optional.of(tacticalInsn);
            if (insn == end) return Optional.empty();
        }

        // We've reached the end of the instruction list but have not passed the "end" instruction
        throw new IllegalArgumentException("Illegal range");
    }

    /**
     * Find the last instruction within a range of instructions that has been converted.
     *
     * @param start first instruction of the range
     * @param end last instruction of the range (exclusive)
     * @return the found instruction or {@link Optional#empty()} if the range contains only dead code
     */
    private Optional<me.aki.tactical.dex.insn.Instruction> lastNonDeadInsnInRange(Instruction start, Instruction end) {
        InsertList<Instruction> insns = this.insnIndex.getInstructions();
        for (Instruction insn = insns.getPrevious(end); insn != null; insn = insns.getPrevious(insn)) {
            me.aki.tactical.dex.insn.Instruction tacticalInsn = this.convertedInsns.get(insn);
            if (tacticalInsn != null) return Optional.of(tacticalInsn);
            if (insn == start) return Optional.empty();
        }

        // We've reached the begin of the instruction list but have not passed the "start" instruction yet
        throw new IllegalArgumentException("Illegal range");
    }

    private me.aki.tactical.dex.insn.Instruction convertedInstructionOf(Instruction insn) {
        while (nonConvertedInstructions.contains(insn)) {
            insn = this.insnIndex.getInstructions().getNext(insn);
        }

        return this.convertedInsns.get(insn);
    }

    private void convertDebug() {
        for (DebugItem debugItem : smaliBody.getDebugItems()) {
            Instruction insn = insnIndex.getInstructionByCodeUnit(debugItem.getCodeAddress());
            switch (debugItem.getDebugItemType()) {
                case DebugItemType.START_LOCAL:
                    StartLocal startLocal = (StartLocal) debugItem;
                    //TODO
                    break;

                case DebugItemType.END_LOCAL:
                    EndLocal endLocal = (EndLocal) debugItem;
                    //TODO
                    break;

                case DebugItemType.RESTART_LOCAL:
                    RestartLocal restartLocal = (RestartLocal) debugItem;
                    //TODO
                    break;

                case DebugItemType.PROLOGUE_END:
                    PrologueEnd prologueEnd = (PrologueEnd) debugItem;
                    //TODO
                    break;

                case DebugItemType.EPILOGUE_BEGIN:
                    EpilogueBegin epilogueBegin = (EpilogueBegin) debugItem;
                    //TODO
                    break;

                case DebugItemType.SET_SOURCE_FILE:
                    SetSourceFile setSourceFile = (SetSourceFile) debugItem;
                    //TODO
                    break;

                case DebugItemType.LINE_NUMBER:
                    LineNumber lineNumber = (LineNumber) debugItem;
                    //TODO
                    break;

                default:
                    throw new AssertionError();
            }
        }
    }

    private void doTyping() {
        DexCfgGraph cfgGraph = new DexCfgGraph(body);

        // pre-processing required to make all registers typeable
        new RegisterPartitioner(cfgGraph).process(body);

        DexTyper dexTyper = new DexTyper(method, cfgGraph);
        dexTyper.typeInstructions();
        dexTyper.typeRegisters();
    }
}
