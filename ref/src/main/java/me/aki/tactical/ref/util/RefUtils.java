package me.aki.tactical.ref.util;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.RefBody;
import me.aki.tactical.ref.Statement;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RefUtils {
    /**
     * Get all references to any statement within one method.
     *
     * @param body the body of the method
     * @return all cell references
     */
    public static Stream<Cell<Statement>> getStatementRefs(RefBody body) {
        Stream<Cell<Statement>> tryCatchRefs = body.getTryCatchBlocks().stream()
                .flatMap(block -> Stream.of(block.getFirstCell(), block.getLastCell(), block.getHandlerCell()));

        Stream<Cell<Statement>> lineRefs = body.getLineNumbers().stream()
                .map(RefBody.LineNumber::getStatementCell);

        return Stream.of(tryCatchRefs, lineRefs).reduce(Stream.empty(), Stream::concat);
    }

    /**
     * Get all references to one certain statement within one method.
     *
     * @param refBody body of the method
     * @param statement references to this statement are requested
     * @return all references of the statements
     */
    public static List<Cell<Statement>> getStatementRefs(RefBody refBody, Statement statement) {
        return getStatementRefs(refBody)
                .filter(cell -> cell.get() == statement)
                .collect(Collectors.toList());
    }

    /**
     * Get a map that zips statement of a method with all cells currently pointing at them.
     *
     * @param refBody body of the method
     * @return statements zip with their cells
     */
    public static Map<Statement, Set<Cell<Statement>>> getStatementRefMap(RefBody refBody) {
        return getStatementRefs(refBody).collect(Collectors.groupingBy(Cell::get, Collectors.toSet()));
    }

    /**
     * Get all cells to any expression within one method.
     *
     * @param refBody body of the method
     * @return all expression cells
     */
    public static Stream<Cell<Expression>> getExpressionCells(RefBody refBody) {
        Stream<Cell<Expression>> stmtRefs = refBody.getStatements().stream()
                .flatMap(stmt -> stmt.getRecursiveReferencedValueCells().stream());

        Stream<Cell<Expression>> tryCatchRefs = refBody.getTryCatchBlocks().stream()
                .map(block -> block.getExceptionLocalCell().cast());

        return Stream.concat(stmtRefs, tryCatchRefs);
    }

    /**
     * Get a map that zips expressions of a method with all cells that currently point a them.
     *
     * @param refBody body of the method
     * @return expressions zipped with their cells
     */
    public static Map<Expression, Set<Cell<Expression>>> getExpressionCellMap(RefBody refBody) {
        return getExpressionCells(refBody).collect(Collectors.groupingBy(Cell::get, Collectors.toSet()));
    }
}
