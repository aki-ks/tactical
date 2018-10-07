package me.aki.tactical.core.annotation;

import java.util.Objects;

public class ByteAnnotationValue implements PrimitiveAnnotationValue {
    private byte value;

    public ByteAnnotationValue(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteAnnotationValue that = (ByteAnnotationValue) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return ByteAnnotationValue.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
