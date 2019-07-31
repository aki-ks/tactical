package me.aki.tactical.core.constant;

import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;

import java.util.Objects;

/**
 * An instance of "java.lang.invoke.MethodHandle".
 */
public class HandleConstant implements BootstrapConstant, PushableConstant, DexConstant, Constant {
    private final Handle handle;

    public HandleConstant(Handle handle) {
        this.handle = handle;
    }

    public Handle getHandle() {
        return handle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandleConstant that = (HandleConstant) o;
        return Objects.equals(handle, that.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }

    @Override
    public String toString() {
        return HandleConstant.class.getSimpleName() + '{' +
                "handle=" + handle +
                '}';
    }

    @Override
    public ObjectType getType() {
        return ObjectType.METHOD_HANDLE;
    }
}
