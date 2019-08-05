package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.invoke.AbstractInvoke;
import me.aki.tactical.ref.stmt.InvokeStmt;

import java.util.List;
import java.util.Objects;

/**
 * An expression for the result of a method invoke.
 *
 * @see InvokeStmt for invokations of methods of <tt>void</tt> type
 */
public class InvokeExpr implements Expression {
    /**
     * Invocation whose result in captured
     */
    private AbstractInvoke invoke;

    public InvokeExpr(AbstractInvoke invoke) {
        this.invoke = invoke;
    }

    public AbstractInvoke getInvoke() {
        return invoke;
    }

    public void setInvoke(AbstractInvoke invoke) {
        this.invoke = invoke;
    }

    @Override
    public Type getType() {
        return invoke.getMethodDescriptor().getReturnType()
                .orElseThrow(() -> new IllegalStateException("Void in InvokeExpr"));
    }

    @Override
    public List<RCell<Expression>> getReadValueCells() {
        return invoke.getReadValueCells();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeExpr that = (InvokeExpr) o;
        return Objects.equals(invoke, that.invoke);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoke);
    }
}
