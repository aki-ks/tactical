package me.aki.tactical.core.annotation;

import java.util.List;

public class ArrayAnnotationValue implements AnnotationValue {
    private List<AnnotationValue> array;

    public ArrayAnnotationValue(List<AnnotationValue> array) {
        this.array = array;
    }

    public List<AnnotationValue> getArray() {
        return array;
    }

    public void setArray(List<AnnotationValue> array) {
        this.array = array;
    }
}
