package me.aki.tactical.ref;

import me.aki.tactical.core.util.Cell;

import java.util.List;

/**
 * An entity that references other expressions
 */
public interface Referencing {
    /**
     * Get cells containing all values referenced by this entity.
     *
     * @return all referenced expressions
     */
    List<Cell<Expression>> getReferencedValues();
}
