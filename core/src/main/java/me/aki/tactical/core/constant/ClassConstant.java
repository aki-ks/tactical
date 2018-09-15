package me.aki.tactical.core.constant;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;

/**
 * An instance of "java.lang.Class".
 */
public class ClassConstant implements Constant {

    private final Path value;

    public ClassConstant(Path value) {
        this.value = value;
    }

    public Path getPath() {
        return value;
    }

    public ObjectType getType() {
        return ObjectType.CLASS;
    }
}
