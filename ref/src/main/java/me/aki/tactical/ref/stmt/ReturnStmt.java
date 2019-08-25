package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Return from a method.
 */
public class ReturnStmt implements Statement {
    /**
     * Value to be returned or empty for <tt>void</tt> method.
     */
    private Optional<Expression> value;

    /**
     * Initialize a ReturnStmt that returns no value as found in <tt>void</tt> methods.
     */
    public ReturnStmt() {
        this(Optional.empty());
    }

    /**
     * Initialize a ReturnStmt that returns a value as found in non-void methods.
     */
    public ReturnStmt(Expression value) {
        this(Optional.of(value));
    }

    public ReturnStmt(Optional<Expression> value) {
        this.value = value;
    }

    public Optional<Expression> getValue() {
        return value;
    }

    public void setValue(Optional<Expression> value) {
        this.value = value;
    }

    public Optional<RWCell<Expression>> getValueCell() {
        return value.map(x -> RWCell.of(() -> value.get(),
                        newValue -> value = Optional.of(newValue), Expression.class));
    }

    @Override
    public Set<RCell<Expression>> getReadValueCells() {
        return getValueCell().stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean continuesExecution() {
        return false;
    }
}
