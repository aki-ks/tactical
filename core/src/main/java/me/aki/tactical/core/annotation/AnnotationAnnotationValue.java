package me.aki.tactical.core.annotation;

import me.aki.tactical.core.Path;

import java.util.Objects;

/**
 * An annotation used as value of a method in a method declaration
 */
public class AnnotationAnnotationValue extends AbstractAnnotation implements AnnotationValue {
    public AnnotationAnnotationValue(Path type) {
        super(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationAnnotationValue that = (AnnotationAnnotationValue) o;
        return Objects.equals(getType(), that.getType()) &&
                Objects.equals(getValues(), that.getValues());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValues());
    }

    @Override
    public String toString() {
        return AnnotationAnnotationValue.class.getSimpleName() + '{' +
                "type=" + getType() +
                ", values=" + getValues() +
                '}';
    }
}
