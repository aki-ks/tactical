package me.aki.tactical.core.handle;

import me.aki.tactical.core.MethodRef;

/**
 * Method Handle where the containing class might be either an interface or a class.
 */
public abstract class AbstractAmbiguousMethodHandle extends AbstractMethodHandle {
    /**
     * Is the method declared within an interface
     */
    private boolean isInterface;

    public AbstractAmbiguousMethodHandle(MethodRef methodRef, boolean isInterface) {
        super(methodRef);
        this.isInterface = isInterface;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }
}
