package me.aki.tactical.core.constant;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;

import java.util.Objects;

/**
 * An instance of "java.lang.Class".
 */
public class ClassConstant implements BootstrapConstant {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassConstant that = (ClassConstant) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return ClassConstant.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
