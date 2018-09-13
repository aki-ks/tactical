package me.aki.tactical.core.annotation;

public class IntAnnotationValue implements PrimitiveAnnotationValue {
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
