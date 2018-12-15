package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.LinkedInsertList;
import me.aki.tactical.core.utils.AbstractCfgGraph;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.MethodImplementation;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class SmaliCfgGraph extends AbstractCfgGraph<Instruction> {
    private final MethodImplementation body;

    private InsertList<Instruction> instructions;

    /**
     * A map from code-unit indices to the corresponding instruction.
     */
    private final Instruction[] instructionByCodeUnit;

    public SmaliCfgGraph(MethodImplementation body) {
        this.body = body;
        this.instructions = new LinkedInsertList<>(body.getInstructions());
        this.instructionByCodeUnit = buildInstructionByCodeUnitMap();
        this.analyze();
    }

    private Instruction[] buildInstructionByCodeUnitMap() {
        List<Instruction> builder = new ArrayList<>();
        for (Instruction instruction : body.getInstructions()) {
            builder.addAll(Collections.nCopies(instruction.getCodeUnits(), instruction));
        }
        return builder.toArray(new Instruction[0]);
    }

    @Override
    protected Instruction getHeadInsn() {
        return instructions.getFirst();
    }

    @Override
    protected Collection<TryCatchBlock> getTryCatchBlocks() {
        List<TryCatchBlock> blocks = new ArrayList<>();
        for (TryBlock<? extends ExceptionHandler> block : body.getTryBlocks()) {
            Instruction start = instructionByCodeUnit[block.getStartCodeAddress()];
            Instruction end = instructionByCodeUnit[block.getStartCodeAddress() + block.getCodeUnitCount()];

            for (ExceptionHandler exceptionHandler : block.getExceptionHandlers()) {
                Instruction handler = instructionByCodeUnit[exceptionHandler.getHandlerCodeAddress()];
                blocks.add(new TryCatchBlock(start, end, handler));
            }
        }
        return blocks;
    }

    @Override
    protected Stream<Instruction> getReachableInstructions(Instruction instruction) {
        Stream<Instruction> succeeding = instruction.getOpcode().canContinue() ?
                Stream.of(instructions.getNext(instruction)) : Stream.empty();

        Stream<Instruction> branchTargets = getBranchTargets(instruction);

        return Stream.concat(succeeding, branchTargets);
    }

    private Stream<Instruction> getBranchTargets(Instruction instruction) {
        switch (instruction.getOpcode()) {
            case GOTO:
                Instruction10t insn10t = (Instruction10t) instruction;
                return Stream.of(instructionByCodeUnit[insn10t.getCodeOffset()]);

            case GOTO_16:
                Instruction20t insn20t = (Instruction20t) instruction;
                return Stream.of(instructionByCodeUnit[insn20t.getCodeOffset()]);

            case GOTO_32:
                Instruction30t insn30t = (Instruction30t) instruction;
                return Stream.of(instructionByCodeUnit[insn30t.getCodeOffset()]);

            case IF_EQ:
            case IF_NE:
            case IF_LE:
            case IF_LT:
            case IF_GE:
            case IF_GT:
                Instruction22t insn22t = (Instruction22t) instruction;
                return Stream.of(instructionByCodeUnit[insn22t.getCodeOffset()]);

            case IF_EQZ:
            case IF_NEZ:
            case IF_LEZ:
            case IF_LTZ:
            case IF_GEZ:
            case IF_GTZ:
                Instruction21t insn21t = (Instruction21t) instruction;
                return Stream.of(instructionByCodeUnit[insn21t.getCodeOffset()]);

            case PACKED_SWITCH:
            case SPARSE_SWITCH:
                Instruction31t insn31t = (Instruction31t) instruction;
                SwitchPayload switchPayload = (SwitchPayload) instructionByCodeUnit[insn31t.getCodeOffset()];
                return switchPayload.getSwitchElements().stream()
                        .map(element -> instructionByCodeUnit[element.getOffset()]);

            default:
                return Stream.empty();
        }
    }

    @Override
    public boolean isDeadCode(Instruction start, Instruction end) {
        Iterator<Instruction> iterator = instructions.iterator(start, end);

        while (iterator.hasNext()) {
            if (!isDeadCode(iterator.next())) {
                return false;
            }
        }

        return true;
    }
}
