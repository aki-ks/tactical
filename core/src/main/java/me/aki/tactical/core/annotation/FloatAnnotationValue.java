package me.aki.tactical.core.annotation;

import java.util.Objects;

public class FloatAnnotationValue implements PrimitiveAnnotationValue {
    private float value;

    public FloatAnnotationValue(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FloatAnnotationValue that = (FloatAnnotationValue) o;
        return Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return FloatAnnotationValue.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
