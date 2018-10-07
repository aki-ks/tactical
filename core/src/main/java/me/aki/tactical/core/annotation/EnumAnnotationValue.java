package me.aki.tactical.core.annotation;

import me.aki.tactical.core.Path;

import java.util.Objects;

/**
 * Representation of an enum constant (e.g. {@code RetentionPolicy.RUNTIME}).
 */
public class EnumAnnotationValue implements AnnotationValue {
    /**
     * Class name of the enum
     */
    private Path type;

    /**
     * Name of the enum constant.
     */
    private String name;

    public EnumAnnotationValue(Path type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Path getType() {
        return type;
    }

    public void setType(Path type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumAnnotationValue that = (EnumAnnotationValue) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return EnumAnnotationValue.class.getSimpleName() + '{' +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
