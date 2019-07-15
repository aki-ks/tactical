package me.aki.tactical.ref.util;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.stmt.AssignStmt;
import me.aki.tactical.ref.stmt.BranchStmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonOperations {
    /**
     * Remove a statement from a method and thereby update all references to that statement.
     *
     * @param body The body of the method that contains the statement
     * @param statement the statement to be removed
     */
    public static void removeStatement(RefBody body, Statement statement) {
        InsertList<Statement> statements = body.getStatements();

        body.getTryCatchBlocks().removeIf(block -> {
            if (block.getHandler() == statement) {
                block.setHandler(statements.getNext(statement));
            }
            return removeFromRange(statements, statement, block.getFirstCell(), block.getLastCell());
        });

        body.getLocalVariables().removeIf(anno ->
                removeFromRange(statements, statement, anno.getStartCell(), anno.getEndCell()));

        body.getLocalVariableAnnotations().removeIf(varAnno -> {
            varAnno.getLocations().removeIf(location ->
                    removeFromRange(statements, statement, location.getStartCell(), location.getEndCell()));

            return varAnno.getLocations().isEmpty();
        });

        body.getLineNumbers().removeIf(lineNumber -> lineNumber.getStatement() == statement);

        statements.stream().filter(stmt -> stmt instanceof BranchStmt)
                .flatMap(stmt -> ((BranchStmt) stmt).getBranchTargetsCells().stream())
                .filter(cell -> cell.get() == statement)
                .forEach(cell -> cell.w(Statement.class).set(statements.getNext(statement)));

        statements.remove(statement);
    }

    /**
     * Update the bounds of a range if it starts or ends with a statement that should be removed.
     *
     * @param statements all statements of the method
     * @param statement the statement to be removed
     * @param start the first statement of the range
     * @param end the last statement of the range
     * @return did the range only contain the removed statement.
     */
    private static boolean removeFromRange(InsertList<Statement> statements, Statement statement, RWCell<Statement> start, RWCell<Statement> end) {
        boolean isStart = start.get() == statement;
        boolean isEnd = end.get() == statement;
        if (isStart && isEnd) {
            return true;
        }

        if (isStart) {
            start.set(statements.getNext(statement));
        }
        if (isEnd) {
            end.set(statements.getPrevious(statement));
        }

        return false;
    }

    /**
     * Remove a local from a RefBody if it is no longer used anywhere within its body.
     *
     * @param body the body containing the local
     * @param local the local to be removed
     * @return was the local actually removed
     */
    public static boolean removeLocal(RefBody body, RefLocal local) {
        if (RefUtils.getExpressionCells(body).anyMatch(cell -> cell.get() == local)) {
            return false;
        }
        return body.getLocals().remove(local);
    }

    /**
     * Get a mapping from locals to all statements that read from it.
     *
     * @param body the body that contains all statements
       * @return map locals to statements that read their value
     */
    public static Map<RefLocal, List<Statement>> getLocalReadMap(RefBody body) {
        final Map<RefLocal, List<Statement>> localReadMap = new HashMap<>();
        for (Statement statement : body.getStatements()) {
            for (Expression expr : statement.getReadValues()) {
                if (expr instanceof RefLocal) {
                    localReadMap.computeIfAbsent((RefLocal) expr, x -> new ArrayList<>()).add(statement);
                }
            }
        }
        return localReadMap;
    }

    /**
     * Get a mapping from locals to all assign statements that write to it.
     *
     * @param body the body that contains all statements
     * @return locals zipped with corresponding assign statements
     */
    public static Map<RefLocal, List<AssignStmt>> getLocalWriteMap(RefBody body) {
        final Map<RefLocal, List<AssignStmt>> localWriteMap = new HashMap<>();
        for (Statement statement : body.getStatements()) {
            statement.getWriteValues().ifPresent(variable -> {
                if (variable instanceof RefLocal) {
                    localWriteMap.computeIfAbsent((RefLocal) variable, x -> new ArrayList<>()).add((AssignStmt) statement);
                }
            });
        }
        return localWriteMap;
    }
}
