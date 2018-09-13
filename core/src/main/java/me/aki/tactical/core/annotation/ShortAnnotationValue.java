package me.aki.tactical.core.annotation;

public class ShortAnnotationValue implements PrimitiveAnnotationValue {
    private short value;

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }
}
