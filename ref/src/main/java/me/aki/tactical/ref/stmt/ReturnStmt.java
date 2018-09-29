package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;

import java.util.List;
import java.util.Optional;

/**
 * Return from a method.
 */
public class ReturnStmt implements Statement {
    /**
     * Value to be returned or empty for <tt>void</tt> method.
     */
    private Optional<Expression> value;

    public ReturnStmt(Optional<Expression> value) {
        this.value = value;
    }

    public Optional<Expression> getValue() {
        return value;
    }

    public void setValue(Optional<Expression> value) {
        this.value = value;
    }

    public Optional<Cell<Expression>> getValueCell() {
        return value.map(x -> Cell.of(() -> value.get(),
                        newValue -> value = Optional.of(newValue), Expression.class));
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return getValueCell().map(List::of).orElse(List.of());
    }
}
