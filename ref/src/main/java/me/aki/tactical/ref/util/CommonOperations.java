package me.aki.tactical.ref.util;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.stmt.AssignStatement;
import me.aki.tactical.ref.stmt.BranchStmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                .forEach(cell -> cell.set(statements.getNext(statement)));

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
    private static boolean removeFromRange(InsertList<Statement> statements, Statement statement, Cell<Statement> start, Cell<Statement> end) {
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
     * Remove a local from a RefBody except is is the 'this' or a parameter local.
     *
     * @param body the body containing the local
     * @param local the local to be removed
     * @return was the local actually removed
     */
    public static boolean removeLocal(RefBody body, RefLocal local) {
        for (RefLocal argumentLocal : body.getArgumentLocals()) {
            if (argumentLocal == local) {
                return false;
            }
        }

        Optional<RefLocal> thisLocal = body.getThisLocal();
        if (thisLocal.isPresent() && thisLocal.get() == local) {
            return false;
        }

        body.getLocals().remove(local);
        return true;
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
    public static Map<RefLocal, List<AssignStatement>> getLocalWriteMap(RefBody body) {
        final Map<RefLocal, List<AssignStatement>> localWriteMap = new HashMap<>();
        for (Statement statement : body.getStatements()) {
            statement.getWriteValues().ifPresent(variable -> {
                if (variable instanceof RefLocal) {
                    localWriteMap.computeIfAbsent((RefLocal) variable, x -> new ArrayList<>()).add((AssignStatement) statement);
                }
            });
        }
        return localWriteMap;
    }
}
