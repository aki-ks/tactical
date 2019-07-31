package me.aki.tactical.dex.utils;

import me.aki.tactical.core.utils.AbstractCfgGraph;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.insn.BranchInstruction;
import me.aki.tactical.dex.insn.Instruction;

import java.util.Collection;
import java.util.Iterator;
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
            Stream<Instruction> branchTargets = ((BranchInstruction) succeedingInsn).getBranchTargets().stream();
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
}
