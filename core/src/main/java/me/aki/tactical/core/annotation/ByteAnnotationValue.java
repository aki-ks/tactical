package me.aki.tactical.core.annotation;

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
}
