package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.Objects;

/**
 * Invoke a statically known method.
 */
public abstract class AbstractConcreteInvoke extends AbstractInvoke {
    /**
     * Method that will be invoked
     */
    private MethodRef method;

    public AbstractConcreteInvoke(MethodRef method, List<Expression> arguments) {
        super(arguments);
        this.method = method;
    }

    public MethodRef getMethod() {
        return method;
    }

    public void setMethod(MethodRef method) {
        this.method = method;
    }

    @Override
    public MethodDescriptor getMethodDescriptor() {
        return new MethodDescriptor(method.getArguments(), method.getReturnType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractConcreteInvoke that = (AbstractConcreteInvoke) o;
        return Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), method);
    }
}
