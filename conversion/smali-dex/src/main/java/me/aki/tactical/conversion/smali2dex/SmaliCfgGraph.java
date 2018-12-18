package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.core.utils.AbstractCfgGraph;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchPayload;
import org.jf.dexlib2.iface.instruction.formats.Instruction10t;
import org.jf.dexlib2.iface.instruction.formats.Instruction20t;
import org.jf.dexlib2.iface.instruction.formats.Instruction21t;
import org.jf.dexlib2.iface.instruction.formats.Instruction22t;
import org.jf.dexlib2.iface.instruction.formats.Instruction30t;
import org.jf.dexlib2.iface.instruction.formats.Instruction31t;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class SmaliCfgGraph extends AbstractCfgGraph<Instruction> {
    private final InstructionIndex insnIndex;
    private final List<? extends TryBlock<? extends ExceptionHandler>> exceptionHandlers;

    public SmaliCfgGraph(InstructionIndex insnIndex, List<? extends TryBlock<? extends ExceptionHandler>> exceptionHandlers) {
        this.insnIndex = insnIndex;
        this.exceptionHandlers = exceptionHandlers;

        this.analyze();
    }

    @Override
    protected Instruction getHeadInsn() {
        return insnIndex.getInstructions().getFirst();
    }

    @Override
    protected Collection<TryCatchBlock> getTryCatchBlocks() {
        List<TryCatchBlock> blocks = new ArrayList<>();
        for (TryBlock<? extends ExceptionHandler> block : exceptionHandlers) {
            Instruction start = insnIndex.getInstructionByCodeUnit(block.getStartCodeAddress());
            Instruction end = insnIndex.getInstructionByCodeUnit(block.getStartCodeAddress() + block.getCodeUnitCount());

            for (ExceptionHandler exceptionHandler : block.getExceptionHandlers()) {
                Instruction handler = insnIndex.getInstructionByCodeUnit(exceptionHandler.getHandlerCodeAddress());
                blocks.add(new TryCatchBlock(start, end, handler));
            }
        }
        return blocks;
    }

    @Override
    protected Stream<Instruction> getReachableInstructions(Instruction instruction) {
        Stream<Instruction> succeeding = instruction.getOpcode().canContinue() ?
                Stream.of(insnIndex.getInstructions().getNext(instruction)) : Stream.empty();

        Stream<Instruction> branchTargets = getBranchTargets(instruction);

        return Stream.concat(succeeding, branchTargets);
    }

    private Stream<Instruction> getBranchTargets(Instruction instruction) {
        switch (instruction.getOpcode()) {
            case GOTO:
                Instruction10t insn10t = (Instruction10t) instruction;
                return Stream.of(insnIndex.getOffsetInstruction(instruction, insn10t.getCodeOffset()));

            case GOTO_16:
                Instruction20t insn20t = (Instruction20t) instruction;
                return Stream.of(insnIndex.getOffsetInstruction(instruction, insn20t.getCodeOffset()));

            case GOTO_32:
                Instruction30t insn30t = (Instruction30t) instruction;
                return Stream.of(insnIndex.getOffsetInstruction(instruction, insn30t.getCodeOffset()));

            case IF_EQ:
            case IF_NE:
            case IF_LE:
            case IF_LT:
            case IF_GE:
            case IF_GT:
                Instruction22t insn22t = (Instruction22t) instruction;
                return Stream.of(insnIndex.getOffsetInstruction(instruction, insn22t.getCodeOffset()));

            case IF_EQZ:
            case IF_NEZ:
            case IF_LEZ:
            case IF_LTZ:
            case IF_GEZ:
            case IF_GTZ:
                Instruction21t insn21t = (Instruction21t) instruction;
                return Stream.of(insnIndex.getOffsetInstruction(instruction, insn21t.getCodeOffset()));

            case PACKED_SWITCH:
            case SPARSE_SWITCH:
                Instruction31t insn31t = (Instruction31t) instruction;
                SwitchPayload switchPayload = (SwitchPayload) insnIndex.getOffsetInstruction(instruction, insn31t.getCodeOffset());
                return switchPayload.getSwitchElements().stream()
                        .map(element -> insnIndex.getOffsetInstruction(instruction, element.getOffset()));

            default:
                return Stream.empty();
        }
    }

    @Override
    public boolean isDeadCode(Instruction start, Instruction end) {
        Iterator<Instruction> iterator = insnIndex.getInstructions().iterator(start, end);

        while (iterator.hasNext()) {
            if (!isDeadCode(iterator.next())) {
                return false;
            }
        }

        return true;
    }
}
