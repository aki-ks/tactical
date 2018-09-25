package me.aki.tactical.core.constant;

import me.aki.tactical.core.Handle;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;

import java.util.Objects;

/**
 * An instance of "java.lang.invoke.MethodHandle".
 */
public class MethodHandleConstant implements Constant, BootstrapConstant {
    private final Handle handle;

    public MethodHandleConstant(Handle handle) {
        this.handle = handle;
    }

    public Handle getHandle() {
        return handle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodHandleConstant that = (MethodHandleConstant) o;
        return Objects.equals(handle, that.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }

    @Override
    public String toString() {
        return MethodHandleConstant.class.getSimpleName() + '{' +
                "handle=" + handle +
                '}';
    }

    @Override
    public ObjectType getType() {
        return new ObjectType(Path.of("java", "lang", "invoke", "MethodHandle"));
    }
}
