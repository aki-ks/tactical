package me.aki.tactical.core.annotation;

public class BooleanAnnotationValue implements PrimitiveAnnotationValue {
    private boolean value;

    public BooleanAnnotationValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
