package me.aki.tactical.core.annotation;

import java.util.Objects;

public class IntAnnotationValue implements PrimitiveAnnotationValue {
    private int value;

    public IntAnnotationValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntAnnotationValue that = (IntAnnotationValue) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return IntAnnotationValue.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
