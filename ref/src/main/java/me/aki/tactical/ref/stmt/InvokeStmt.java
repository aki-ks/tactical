package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.expr.InvokeExpr;
import me.aki.tactical.ref.invoke.AbstractInvoke;

import java.util.List;
import java.util.Objects;

/**
 * Invoke a method without storing the return value.
 *
 * @see InvokeExpr to get the return value
 */
public class InvokeStmt implements Statement {
    private AbstractInvoke invoke;

    public InvokeStmt(AbstractInvoke invoke) {
        this.invoke = invoke;
    }

    public AbstractInvoke getInvoke() {
        return invoke;
    }

    public void setInvoke(AbstractInvoke invoke) {
        this.invoke = invoke;
    }

    @Override
    public List<Cell<Expression>> getReferencedValueCells() {
        return invoke.getReferencedValueCells();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeStmt that = (InvokeStmt) o;
        return Objects.equals(invoke, that.invoke);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoke);
    }
}
