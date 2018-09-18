package me.aki.tactical.core.annotation;

public class FloatAnnotationValue implements PrimitiveAnnotationValue {
    private float value;

    public FloatAnnotationValue(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
