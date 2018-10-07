package me.aki.tactical.core.annotation;

import java.util.Objects;

public class LongAnnotationValue implements PrimitiveAnnotationValue {
    private long value;

    public LongAnnotationValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongAnnotationValue that = (LongAnnotationValue) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return LongAnnotationValue.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
