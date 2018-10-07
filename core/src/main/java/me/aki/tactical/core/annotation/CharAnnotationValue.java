package me.aki.tactical.core.annotation;

import java.util.Objects;

public class CharAnnotationValue implements PrimitiveAnnotationValue {
    private char value;

    public CharAnnotationValue(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharAnnotationValue that = (CharAnnotationValue) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return CharAnnotationValue.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
