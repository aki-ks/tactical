package me.aki.tactical.ref;

import me.aki.tactical.core.util.RWCell;

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
