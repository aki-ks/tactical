package me.aki.tactical.core.annotation;

import me.aki.tactical.core.Path;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * An annotation used as value of a method in a method declaration
 */
public class AnnotationAnnotationValue extends AbstractAnnotation implements AnnotationValue {
    public AnnotationAnnotationValue(Path type) {
        super(type);
    }

    public AnnotationAnnotationValue(Path type, LinkedHashMap<String, AnnotationValue> values) {
        super(type, values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AnnotationAnnotationValue that = (AnnotationAnnotationValue) o;
        return Objects.equals(getType(), that.getType()) &&
                Objects.equals(getValues(), that.getValues());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getType(), getValues());
    }

    @Override
    public String toString() {
        return AnnotationAnnotationValue.class.getSimpleName() + '{' +
                "type=" + getType() +
                ", values=" + getValues() +
                '}';
    }
}
