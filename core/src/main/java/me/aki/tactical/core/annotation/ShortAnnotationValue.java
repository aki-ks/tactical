package me.aki.tactical.core.annotation;

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
}
