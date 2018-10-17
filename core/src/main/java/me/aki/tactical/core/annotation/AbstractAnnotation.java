package me.aki.tactical.core.annotation;

import me.aki.tactical.core.Path;

import java.util.LinkedHashMap;
import java.util.Objects;

public class AbstractAnnotation {
    /**
     * Class of this annotation.
     */
    private Path type;

    /**
     * Values of all methods of the annotation.
     */
    private LinkedHashMap<String, AnnotationValue> values;

    public AbstractAnnotation(Path type) {
        this.type = type;
        this.values = new LinkedHashMap<>();
    }

    public AbstractAnnotation(Path type, LinkedHashMap<String, AnnotationValue> values) {
        this.type = type;
        this.values = values;
    }

    public Path getType() {
        return type;
    }

    public void setType(Path type) {
        this.type = type;
    }

    public LinkedHashMap<String, AnnotationValue> getValues() {
        return values;
    }

    public void setValues(LinkedHashMap<String, AnnotationValue> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAnnotation that = (AbstractAnnotation) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, values);
    }
}
