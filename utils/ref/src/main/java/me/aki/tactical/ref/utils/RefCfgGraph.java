package me.aki.tactical.ref.utils;

import me.aki.tactical.core.utils.AbstractCfgGraph;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.stmt.BranchStmt;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RefCfgGraph extends AbstractCfgGraph<Statement> {
    private final RefBody body;

    public RefCfgGraph(RefBody body) {
        this.body = body;
    }

    public RefBody getBody() {
        return body;
    }

    @Override
    protected Statement getHeadInsn() {
        return body.getStatements().getFirst();
    }

    @Override
    protected Collection<TryCatchBlock> getTryCatchBlocks() {
        return body.getTryCatchBlocks().stream()
                .map(block -> new TryCatchBlock(block.getFirst(), block.getLast(), block.getHandler()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected Stream<Statement> getReachableInstructions(Statement statement) {
        Stream<Statement> nextInsn = statement.continuesExecution() ?
                Stream.of(body.getStatements().getNext(statement)) : Stream.empty();

        Stream<Statement> branchTargets = statement instanceof BranchStmt ?
                ((BranchStmt) statement).getBranchTargets().stream() : Stream.empty();

        return Stream.concat(nextInsn, branchTargets);
    }

    @Override
    public boolean isDeadCode(Statement start, Statement end) {
        Iterator<Statement> iterator = body.getStatements().iterator(start, end);

        while (iterator.hasNext()) {
            if (!isDeadCode(iterator.next())) {
                return false;
            }
        }

        return true;
    }
}
