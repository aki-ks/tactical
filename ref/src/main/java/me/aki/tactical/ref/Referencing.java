package me.aki.tactical.ref;

import me.aki.tactical.core.util.RCell;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An entity that references other expressions
 */
public interface Referencing {
    /**
     * Get cells containing all expressions that this entity reads from.
     *
     * @return cells of all read expressions
     */
    List<RCell<Expression>> getReadValueCells();

    /**
     * Get all expressions that this entity reads from.
     *
     * @return all read expressions
     */
    default List<Expression> getReadValues() {
        return getReadValueCells().stream()
                .map(RCell::get)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get all expressions read by this entity and also expressions read by those.
     *
     * @return cells of all referenced expressions
     */
    default List<Expression> getAllReadValues() {
        class Inner {
            private Stream<Expression> getRecursiveReferencedValueStream(Referencing ref) {
                return ref.getReadValueCells().stream()
                        .map(RCell::get)
                        .flatMap(expr -> Stream.concat(Stream.of(expr), getRecursiveReferencedValueStream(expr)));
            }
        }

        return new Inner().getRecursiveReferencedValueStream(this).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get all expressions that this entity reads from and also expressions read by those.
     *
     * @return cells of all referenced expressions
     */
    default List<RCell<Expression>> getAllReadValueCells() {
        class Inner {
            private Stream<RCell<Expression>> getRecursiveReferencedValueCellStream(Referencing ref) {
                return ref.getReadValueCells().stream()
                        .flatMap(refCell -> Stream.concat(Stream.of(refCell), getRecursiveReferencedValueCellStream(refCell.get())));
            }
        }

        return new Inner().getRecursiveReferencedValueCellStream(this).collect(Collectors.toUnmodifiableList());
    }
}
