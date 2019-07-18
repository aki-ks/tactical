package me.aki.tactical.ref;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;

import java.util.List;
import java.util.Optional;

public interface Statement extends Referencing {
    /**
     * May the instruction followed by this one be ever executed?
     *
     * @return false if the succeeding instruction is unreachable
     */
    default boolean continuesExecution() {
        return true;
    }

    /**
     * Return all values that this statements reads from. These are all values returned by
     * {@link #getRecursiveReferencedValues()} except the variable in an assign statement.
     *
     * @return all expressions that this statement reads from.
     */
    default List<Expression> getReadValues() {
        return this.getRecursiveReferencedValues();
    }

    /**
     * Return cells containing all values that this statements reads from. These are all values
     * returned by {@link #getRecursiveReferencedValueCells()} except the variable in an assign statement.
     *
     * @return cells containing all expressions that this statement reads from.
     */
    default List<RCell<Expression>> getReadValueCells() {
        return this.getRecursiveReferencedValueCells();
    }

    /**
     * Get the variable that this statement writes to.
     * This can only be the variable of an assign statement.
     *
     * @return the variable that this statement writes into or empty
     */
    default Optional<Variable> getWriteValue() {
        return Optional.empty();
    }

    /**
     * Get a cell containing the variable that this statement writes to.
     * This can only be the variable of an assign statement.
     *
     * @return cell containing the variable that this statement writes into or empty
     */
    default Optional<RWCell<Variable>> getWriteValueCell() {
        return Optional.empty();
    }
}
