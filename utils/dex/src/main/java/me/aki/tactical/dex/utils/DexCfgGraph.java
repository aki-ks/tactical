package me.aki.tactical.dex.utils;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.core.utils.AbstractCfgGraph;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.insn.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DexCfgGraph extends AbstractCfgGraph<Instruction> {
    private final DexBody body;

    public DexCfgGraph(DexBody body) {
        this.body = body;
        this.analyze();
    }

    public DexBody getBody() {
        return body;
    }

    @Override
    protected Instruction getHeadInsn() {
        return body.getInstructions().getFirst();
    }

    @Override
    protected Collection<TryCatchBlock> getTryCatchBlocks() {
        return body.getTryCatchBlocks().stream()
                .flatMap(block -> block.getHandlers().stream()
                        .map(handler -> new TryCatchBlock(block.getStart(), block.getEnd(), handler.getHandler())))
                .collect(Collectors.toList());
    }

    @Override
    protected Stream<Instruction> getReachableInstructions(Instruction instruction) {
        Stream<Instruction> succeedingInsn = instruction.continuesExecution() ?
                Stream.of(body.getInstructions().getNext(instruction)) : Stream.empty();

        if (instruction instanceof BranchInstruction) {
            Stream<Instruction> branchTargets = ((BranchInstruction) instruction).getBranchTargets().stream();
            return Stream.concat(succeedingInsn, branchTargets);
        } else {
            return succeedingInsn;
        }
    }

    @Override
    public boolean isDeadCode(Instruction start, Instruction end) {
        Iterator<Instruction> iterator = body.getInstructions().iterator(start, end);

        while (iterator.hasNext()) {
            if (!isDeadCode(iterator.next())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Remove an instruction from the method and the CfgGraph.
     *
     * Note: Instructions of type {@link ReturnInstruction}, {@link ReturnVoidInstruction} or {@link ThrowInstruction} cannot be removed.
     * Since no instruction is executed after them, branches to that instruction cannot be redirected to its successor.
     *
     * @param node the non-method-exiting instruction to be removed
     */
    public void remove(Node node) {
        Instruction insn = node.getInstruction();
        Instruction successor = getSucceedingInstruction(insn);
        Instruction predecessor = getPrecedingInstruction(insn);

        for (Node preceding : node.getPreceding()) {
            Instruction precedingInsn = preceding.getInstruction();
            if (precedingInsn instanceof BranchInstruction) {
                List<RWCell<Instruction>> targetCells = ((BranchInstruction) precedingInsn).getBranchTargetCells();
                for (RWCell<Instruction> targetCell : targetCells) {
                    if (targetCell.get() == insn) {
                        targetCell.set(successor);
                    }
                }
            } else if (precedingInsn != predecessor) {
                // The instruction should only be reachable by its
                // predecessor or by instructions that branch to it.
                throw new IllegalStateException();
            }
        }

        nodes.remove(insn);
        body.getInstructions().remove(insn);
    }

    private Instruction getSucceedingInstruction(Instruction insn) {
        if (insn.continuesExecution()) {
            return body.getInstructions().getNext(insn);
        } else {
            if (insn instanceof ReturnInstruction || insn instanceof ReturnVoidInstruction || insn instanceof ThrowInstruction) {
                throw new IllegalArgumentException("Cannot remove method exiting instruction");
            } else if (insn instanceof GotoInstruction) {
                return ((GotoInstruction) insn).getTarget();
            } else {
                throw new AssertionError();
            }
        }
    }

    private Instruction getPrecedingInstruction(Instruction insn) {
        Instruction prevInsn = body.getInstructions().getPrevious(insn);
        return prevInsn.continuesExecution() ? prevInsn : null;
    }
}
