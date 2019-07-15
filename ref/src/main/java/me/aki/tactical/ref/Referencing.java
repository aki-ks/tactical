package me.aki.tactical.ref;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;

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
    List<RCell<Expression>> getReferencedValueCells();

    /**
     * Get all expressions referenced by this entity.
     *
     * @return all references expressions
     */
    default List<Expression> getReferencedValues() {
        return getReferencedValueCells().stream()
                .map(RCell::get)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get all expressions referenced by this entity and also add their used references.
     *
     * @return cells of all (recursive collected) referenced expressions
     */
    default List<Expression> getRecursiveReferencedValues() {
        return getRecursiveReferencedValueStream().collect(Collectors.toUnmodifiableList());
    }

    private Stream<Expression> getRecursiveReferencedValueStream() {
        return getReferencedValueCells().stream()
                .map(RCell::get)
                .flatMap(expr -> Stream.concat(Stream.of(expr), ((Referencing) expr).getRecursiveReferencedValueStream()));
    }

    /**
     * Get all expressions referenced by this entity and also add their used references.
     *
     * @return cells of all (recursive collected) referenced expressions
     */
    default List<RCell<Expression>> getRecursiveReferencedValueCells() {
        return getRecursiveReferencedValueCellStream().collect(Collectors.toUnmodifiableList());
    }

    private Stream<RCell<Expression>> getRecursiveReferencedValueCellStream() {
        return getReferencedValueCells().stream()
                .flatMap(refCell -> Stream.concat(Stream.of(refCell), ((Referencing) refCell.get()).getRecursiveReferencedValueCellStream()));
    }
}
