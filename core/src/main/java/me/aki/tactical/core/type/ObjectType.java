package me.aki.tactical.core.type;

import me.aki.tactical.core.Path;

import java.util.Objects;

/**
 * Type of a class/interface/enum.
 */
public class ObjectType implements RefType {
    public static final ObjectType OBJECT = new ObjectType(Path.OBJECT);
    public static final ObjectType STRING = new ObjectType(Path.STRING);
    public static final ObjectType CLASS = new ObjectType(Path.CLASS);
    public static final ObjectType THROWABLE = new ObjectType(Path.THROWABLE);
    public static final ObjectType METHOD_HANDLE = new ObjectType(Path.METHOD_HANDLE);
    public static final ObjectType METHOD_TYPE = new ObjectType(Path.METHOD_TYPE);

    private final Path name;

    public ObjectType(Path name) {
        this.name = name;
    }

    public Path getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectType that = (ObjectType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return ObjectType.class.getSimpleName() + '{' +
                "name=" + name +
                '}';
    }
}
