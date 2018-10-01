package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.ref.Expression;

import java.util.List;

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
}
