package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.dex.Register;

import java.util.List;

/**
 * Invoke a statically known method.
 */
public abstract class ConcreteInvoke extends Invoke {
    /**
     * Method that gets invoked.
     */
    private MethodRef method;

    public ConcreteInvoke(MethodRef method, List<Register> arguments) {
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
    public MethodDescriptor getDescriptor() {
        return method.getDescriptor();
    }
}
