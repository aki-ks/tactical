package me.aki.tactical.core.annotation;

import java.util.Objects;

public class ShortAnnotationValue implements PrimitiveAnnotationValue {
    private short value;

    public ShortAnnotationValue(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortAnnotationValue that = (ShortAnnotationValue) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return ShortAnnotationValue.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
