package me.aki.tactical.stack.invoke;

import me.aki.tactical.core.MethodRef;

/**
 * Invoke an instance method of an interface.
 */
public class InterfaceInvoke extends AbstractInstanceInvoke {
    public InterfaceInvoke(MethodRef method) {
        super(method);
    }
}
