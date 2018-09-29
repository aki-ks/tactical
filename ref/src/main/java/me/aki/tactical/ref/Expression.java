package me.aki.tactical.ref;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;

import java.util.List;

public interface Expression {
    /**
     * The type of this value
     */
    Type getType();

    /**
     * Get cells of all values referenced by this expression.
     *
     * @return other expressions referenced by this expression
     */
    List<Cell<Expression>> getReferencedValues();
}
