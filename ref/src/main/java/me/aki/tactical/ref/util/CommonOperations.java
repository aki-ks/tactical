package me.aki.tactical.ref.util;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.stmt.BranchStmt;

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
}
