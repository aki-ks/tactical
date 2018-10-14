package me.aki.tactical.stack.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;

import java.util.Objects;

/**
 * Invoke a statically known method
 */
public class AbstractConcreteInvoke implements Invoke {
    /**
     * Method that gets invoked
     */
    private MethodRef method;

    public AbstractConcreteInvoke(MethodRef method) {
        this.method = method;
    }

    public MethodRef getMethod() {
        return method;
    }

    public void setMethod(MethodRef method) {
        this.method = method;
    }

    @Override
    public MethodDescriptor getDescriptor() {
        return method.getDescriptor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractConcreteInvoke that = (AbstractConcreteInvoke) o;
        return Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method);
    }
}
