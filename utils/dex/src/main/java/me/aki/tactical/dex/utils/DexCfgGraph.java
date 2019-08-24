package me.aki.tactical.dex.utils;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.core.utils.AbstractCfgGraph;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.insn.*;

import java.util.*;
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
     * Note: Only instructions that {@link Instruction#continuesExecution() continueExecution} can be removed.
     *
     * @param node the instruction to be removed
     */
    public void remove(Node node) {
        Instruction insn = node.getInstruction();
        Instruction successor = getSucceedingInstruction(insn);
        Optional<Instruction> predecessor = getPrecedingInstruction(insn);

        Node successorNode = getNode(successor);
        Optional<Node> predecessorNodeOpt = predecessor.map(this::getNode);

        // Let all instructions that branch to the instruction
        // branch to the succeeding instruction instead.
        for (Node preceding : node.getPreceding()) {
            preceding.getSucceeding().remove(node);

            preceding.getSucceeding().add(successorNode);
            successorNode.getPreceding().add(preceding);

            if (preceding.getInstruction() instanceof BranchInstruction) {
                ((BranchInstruction) preceding.getInstruction()).getBranchTargetCells().stream()
                        .filter(targetCell -> targetCell.get() == insn)
                        .forEach(targetCell -> targetCell.set(successor));
            } else if (!(predecessor.isPresent() && predecessor.get() == preceding.getInstruction())) {
                // The instruction should only be reachable by its predecessor or instructions that branch to it.
                throw new IllegalStateException();
            }
        }

        // Remove the instruction as predecessor of all nodes that it branches to
        for (Node succeeding : node.getSucceeding()) {
            succeeding.getPreceding().remove(node);
        }

        predecessorNodeOpt.ifPresent(predecessorNode -> successorNode.getPreceding().add(predecessorNode));

        // Update try/catch blocks
        boolean isExceptionHandler = getHandlerNodes().remove(node);
        if (isExceptionHandler) {
            getHandlerNodes().add(successorNode);

            body.getTryCatchBlocks().stream()
                    .flatMap(tryCatchBlock -> tryCatchBlock.getHandlers().stream())
                    .filter(h -> h.getHandler() == insn)
                    .forEach(h -> h.setHandler(successor));
        }

        nodes.remove(insn);
        body.getInstructions().remove(insn);
    }

    private Instruction getSucceedingInstruction(Instruction insn) {
        if (insn.continuesExecution()) {
            return body.getInstructions().getNext(insn);
        } else {
            throw new IllegalArgumentException("Cannot remove instruction that does not continue the method execution");
        }
    }

    private Optional<Instruction> getPrecedingInstruction(Instruction insn) {
        Instruction prevInsn = body.getInstructions().getPrevious(insn);
        return prevInsn != null && prevInsn.continuesExecution() ? Optional.of(prevInsn) : Optional.empty();
    }

    public void insertBefore(Node locationNode, Instruction insertInsn) {
        Node insertNode = getOrCreateNode(insertInsn);
        Instruction locationInsn = locationNode.getInstruction();

        // All instructions that branch to the insertion location should branch to the inserted instruction instead
        Optional<Instruction> predecessor = getPrecedingInstruction(locationInsn);
        for (Node preceding : locationNode.getPreceding()) {
            preceding.getSucceeding().remove(locationNode);
            preceding.getSucceeding().add(insertNode);
            insertNode.getPreceding().add(preceding);

            if (preceding.getInstruction() instanceof BranchInstruction) {
                List<RWCell<Instruction>> branchTargetCells = ((BranchInstruction) preceding.getInstruction()).getBranchTargetCells();
                for (RWCell<Instruction> branchTargetCell : branchTargetCells) {
                    if (branchTargetCell.get() == locationInsn) {
                        branchTargetCell.set(insertInsn);
                    }
                }
            } else if (!(predecessor.isPresent() && predecessor.get() == preceding.getInstruction())) {
                // The instruction should only be reachable by its predecessor or instructions that branch to it.
                throw new IllegalStateException();
            }
        }

        // Create links between the inserted instruction and its new successor
        locationNode.getPreceding().clear();
        locationNode.getPreceding().add(insertNode);
        insertNode.getSucceeding().add(locationNode);

        // Created links between the inserted instruction and all insns that it branches to
        if (insertInsn instanceof BranchInstruction) {
            for (Instruction branchTarget : ((BranchInstruction) insertInsn).getBranchTargets()) {
                Node branchTargetNode = getNode(branchTarget);
                branchTargetNode.getPreceding().add(insertNode);
                insertNode.getSucceeding().add(branchTargetNode);
            }
        }

        boolean isExceptionHandler = getHandlerNodes().remove(locationNode);
        if (isExceptionHandler) {
            getHandlerNodes().add(insertNode);

            body.getTryCatchBlocks().stream()
                    .flatMap(tryCatchBlock -> tryCatchBlock.getHandlers().stream())
                    .filter(h -> h.getHandler() == locationInsn)
                    .forEach(h -> h.setHandler(insertInsn));
        }

        body.getInstructions().insertBefore(locationInsn, insertInsn);
    }
}
