package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;

import java.util.Set;

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

    public RWCell<Expression> getValueCell() {
        return RWCell.of(this::getValue, this::setValue, Expression.class);
    }

    @Override
    public Set<RCell<Expression>> getReadValueCells() {
        return Set.of(getValueCell());
    }
}
