package me.aki.tactical.core.annotation;

import me.aki.tactical.core.Path;

import java.util.LinkedHashMap;

/**
 * An annotation as declared within classes/methods/field and for types.
 */
public class Annotation extends AbstractAnnotation {
    /**
     * Is this annotation visible at runtime (via reflection api).
     */
    private boolean isRuntimeVisible;

    public Annotation(Path type, boolean isRuntimeVisible) {
        super(type);
        this.isRuntimeVisible = isRuntimeVisible;
    }

    public Annotation(Path type, boolean isRuntimeVisible, LinkedHashMap<String, AnnotationValue> values) {
        super(type, values);
        this.isRuntimeVisible = isRuntimeVisible;
    }

    public boolean isRuntimeVisible() {
        return isRuntimeVisible;
    }

    public void setRuntimeVisible(boolean runtimeVisible) {
        isRuntimeVisible = runtimeVisible;
    }
}
