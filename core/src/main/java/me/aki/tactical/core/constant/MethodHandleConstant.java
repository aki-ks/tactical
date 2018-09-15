package me.aki.tactical.core.constant;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.MethodHandle;
import me.aki.tactical.core.type.ObjectType;

/**
 * An instance of "java.lang.invoke.MethodHandle".
 */
public class MethodHandleConstant implements Constant {
    private final MethodHandle handle;

    public MethodHandleConstant(MethodHandle handle) {
        this.handle = handle;
    }

    public MethodHandle getHandle() {
        return handle;
    }

    @Override
    public ObjectType getType() {
        return new ObjectType(Path.of("java", "lang", "invoke", "MethodHandle"));
    }
}
