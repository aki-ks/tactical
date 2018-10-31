package me.aki.tactical.ref;

import me.aki.tactical.core.util.Cell;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An entity that references other expressions
 */
public interface Referencing {
    /**
     * Get cells containing all expressions referenced by this entity.
     *
     * @return cells of all referenced expressions
     */
    List<Cell<Expression>> getReferencedValueCells();

    /**
     * Get all expressions referenced by this entity.
     *
     * @return all references expressions
     */
    default List<Expression> getReferencedValue() {
        return getReferencedValueCells().stream()
                .map(Cell::get)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get all expressions referenced by this entity and also add their used references.
     *
     * @return cells of all (recursive collected) referenced expressions
     */
    default List<Expression> getRecursiveReferencedValues() {
        return getReferencedValue().stream()
                .flatMap(refCell -> Stream.concat(Stream.of(refCell), refCell.getRecursiveReferencedValues().stream()))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get all expressions referenced by this entity and also add their used references.
     *
     * @return cells of all (recursive collected) referenced expressions
     */
    default List<Cell<Expression>> getRecursiveReferencedValueCells() {
        return getReferencedValueCells().stream()
                .flatMap(refCell -> Stream.concat(Stream.of(refCell), refCell.get().getRecursiveReferencedValueCells().stream()))
                .collect(Collectors.toUnmodifiableList());
    }
}
