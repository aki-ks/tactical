package me.aki.tactical.core.annotation;

import java.util.Objects;

public class DoubleAnnotationValue implements PrimitiveAnnotationValue {
    private double value;

    public DoubleAnnotationValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleAnnotationValue that = (DoubleAnnotationValue) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return DoubleAnnotationValue.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
