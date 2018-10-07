package me.aki.tactical.core.annotation;

import java.util.Objects;

public class BooleanAnnotationValue implements PrimitiveAnnotationValue {
    private boolean value;

    public BooleanAnnotationValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanAnnotationValue that = (BooleanAnnotationValue) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return BooleanAnnotationValue.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
