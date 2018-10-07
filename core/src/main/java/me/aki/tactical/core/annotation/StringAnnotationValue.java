package me.aki.tactical.core.annotation;

import java.util.Objects;

public class StringAnnotationValue implements AnnotationValue {
    private String value;

    public StringAnnotationValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringAnnotationValue that = (StringAnnotationValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return StringAnnotationValue.class.getSimpleName() + '{' +
                "value='" + value + '\'' +
                '}';
    }
}
