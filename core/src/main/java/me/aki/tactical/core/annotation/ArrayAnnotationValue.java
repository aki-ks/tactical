package me.aki.tactical.core.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArrayAnnotationValue implements AnnotationValue {
    private List<AnnotationValue> array;

    public ArrayAnnotationValue() {
        this(new ArrayList<>());
    }

    public ArrayAnnotationValue(List<AnnotationValue> array) {
        this.array = array;
    }

    public List<AnnotationValue> getArray() {
        return array;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayAnnotationValue that = (ArrayAnnotationValue) o;
        return Objects.equals(array, that.array);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array);
    }

    @Override
    public String toString() {
        return ArrayAnnotationValue.class.getSimpleName() + '{' +
                "array=" + array +
                '}';
    }
}
