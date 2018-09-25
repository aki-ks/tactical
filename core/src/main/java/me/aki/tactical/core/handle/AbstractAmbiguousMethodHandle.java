package me.aki.tactical.core.handle;

import me.aki.tactical.core.MethodRef;

import java.util.Objects;

/**
 * Method Handle where the containing class might be either an interface or a class.
 */
public abstract class AbstractAmbiguousMethodHandle extends AbstractMethodHandle {
    /**
     * Is the class that contains the method an interface
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        me.aki.tactical.core.handle.AbstractAmbiguousMethodHandle that = (me.aki.tactical.core.handle.AbstractAmbiguousMethodHandle) o;
        return isInterface == that.isInterface;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isInterface);
    }

    @Override
    public String toString() {
        return me.aki.tactical.core.handle.AbstractAmbiguousMethodHandle.class.getSimpleName() + '{' +
                "methodRef=" + getMethodRef() +
                ", isInterface=" + isInterface +
                '}';
    }
}
