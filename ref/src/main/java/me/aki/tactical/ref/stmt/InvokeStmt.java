package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.expr.InvokeExpr;
import me.aki.tactical.ref.invoke.AbstractInvoke;

import java.util.List;

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
    public List<Cell<Expression>> getReferencedValues() {
        return invoke.getReferencedValues();
    }
}
