package me.aki.tactical.stack.invoke;

import me.aki.tactical.core.MethodRef;

/**
 * Invoke a method on a class instance.
 *
 * For some cases {@link SpecialInvoke} must be used instead.
 */
public class VirtualInvoke extends AbstractInstanceInvoke {
    public VirtualInvoke(MethodRef method) {
        super(method);
    }
}
