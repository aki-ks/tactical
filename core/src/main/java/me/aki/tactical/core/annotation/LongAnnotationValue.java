package me.aki.tactical.core.annotation;

public class LongAnnotationValue implements PrimitiveAnnotationValue {
    private long value;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
