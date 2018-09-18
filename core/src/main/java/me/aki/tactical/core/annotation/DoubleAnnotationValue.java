package me.aki.tactical.core.annotation;

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
}
