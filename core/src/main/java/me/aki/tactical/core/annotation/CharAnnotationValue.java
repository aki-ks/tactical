package me.aki.tactical.core.annotation;

public class CharAnnotationValue implements PrimitiveAnnotationValue {
    private char value;

    public CharAnnotationValue(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }
}
