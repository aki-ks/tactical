package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
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
    private AbstractInvoke invocation;

    public InvokeExpr(AbstractInvoke invocation) {
        this.invocation = invocation;
    }

    public AbstractInvoke getInvocation() {
        return invocation;
    }

    public void setInvocation(AbstractInvoke invocation) {
        this.invocation = invocation;
    }

    @Override
    public Type getType() {
        return invocation.getMethodDescriptor().getReturnType()
                .orElseThrow(() -> new IllegalStateException("Void in InvokeExpr"));
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return invocation.getReferencedValues();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeExpr that = (InvokeExpr) o;
        return Objects.equals(invocation, that.invocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invocation);
    }
}
