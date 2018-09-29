package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.invoke.Invoke;
import me.aki.tactical.ref.stmt.InvokeStmt;

import java.util.List;

/**
 * An expression for the result of a method invoke.
 *
 * @see InvokeStmt for invokations of methods of <tt>void</tt> type
 */
public class InvokeExpr implements Expression {
    /**
     * Invocation whose result in captured
     */
    private Invoke invocation;

    public InvokeExpr(Invoke invocation) {
        this.invocation = invocation;
    }

    public Invoke getInvocation() {
        return invocation;
    }

    public void setInvocation(Invoke invocation) {
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
}
