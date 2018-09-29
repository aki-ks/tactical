package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;

import java.util.List;

public class AbstractUnaryStmt implements Statement {
    private Expression value;

    public AbstractUnaryStmt(Expression value) {
        this.value = value;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public Cell<Expression> getValueCell() {
        return Cell.of(this::getValue, this::setValue, Expression.class);
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of(getValueCell());
    }
}
