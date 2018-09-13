package me.aki.tactical.core.annotation;

public class StringAnnotationValue implements AnnotationValue {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
