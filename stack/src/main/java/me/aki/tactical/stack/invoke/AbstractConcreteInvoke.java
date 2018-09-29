package me.aki.tactical.stack.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;

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
}
