package me.aki.tactical.core.handle;

import me.aki.tactical.core.InvokableMethodRef;
import me.aki.tactical.core.MethodRef;

import java.util.Objects;

/**
 * Method Handle where the containing class might be either an interface or a class.
 */
public abstract class AbstractAmbiguousMethodHandle extends AbstractMethodHandle {
    public AbstractAmbiguousMethodHandle(InvokableMethodRef methodRef) {
        super(methodRef);
    }

    @Override
    public InvokableMethodRef getMethodRef() {
        return (InvokableMethodRef) super.getMethodRef();
    }
}
