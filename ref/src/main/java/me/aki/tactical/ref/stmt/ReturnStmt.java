package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;

import java.util.List;
import java.util.Objects;
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

    @Override
    public boolean continuesExecution() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnStmt stmt = (ReturnStmt) o;
        return Objects.equals(value, stmt.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
