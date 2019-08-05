package me.aki.tactical.conversion.stack2ref.postprocessor;

import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.stmt.AssignStmt;
import me.aki.tactical.ref.util.CommonOperations;

import java.util.List;
import java.util.Map;

/**
 * A PostProcessor that inlines values stored in a local and that are only referenced by the immediately following statement.
 *
 * Here's pseudocode for an example that would get inlined:
 * <pre><code>
 *     local = "Hello World;
 *     System.out.println(local);
 * </code></pre>
 */
public class DirectReuseInliningPostProcessor implements PostProcessor {
    @Override
    public void process(RefBody body) {
        final InsertList<Statement> statements = body.getStatements();
        final Map<RefLocal, List<Statement>> localReadMap = CommonOperations.getLocalReadMap(body);
        final Map<RefLocal, List<AssignStmt>> localWriteMap = CommonOperations.getLocalWriteMap(body);

        List.copyOf(body.getLocals()).forEach(local -> {
            List<Statement> readingStatements = localReadMap.getOrDefault(local, List.of());
            List<AssignStmt> writingStatements = localWriteMap.getOrDefault(local, List.of());
            if (writingStatements.size() != 1 || readingStatements.size() != 1) {
                // The local is read or written none or multiple times, so we cannot inline it.
                return;
            }

            Statement readingStatement = readingStatements.get(0);
            AssignStmt writingStatement = writingStatements.get(0);
            if (statements.getNext(writingStatement) != readingStatement) {
                // The statement that reads from the local does not directly succeed the writing statements.
                return;
            }

            readingStatement.getAllReadValueCells().stream()
                    .filter(cell -> cell.get() == local)
                    .findAny().get()
                    .w(Expression.class).set(writingStatement.getValue());

            CommonOperations.removeStatement(body, writingStatement);
            CommonOperations.removeLocal(body, local);
        });
    }
}
