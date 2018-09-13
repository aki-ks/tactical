package me.aki.tactical.core.annotation;

import me.aki.tactical.core.type.Type;

/**
 * Representation of {@link java.lang.Class} instance.
 */
public class ClassAnnotationValue implements AnnotationValue {
    private Type value;

    public ClassAnnotationValue(Type value) {
        this.value = value;
    }

    public Type getValue() {
        return value;
    }

    public void setValue(Type value) {
        this.value = value;
    }
}
