package me.aki.tactical.stack.invoke;

import me.aki.tactical.core.MethodRef;

/**
 * Invoke a method on an instance of a class
 */
public class AbstractInstanceInvoke extends AbstractConcreteInvoke {
    public AbstractInstanceInvoke(MethodRef method) {
        super(method);
    }
}
