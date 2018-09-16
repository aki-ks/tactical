package me.aki.tactical.core.annotation;

/**
 * An annotation as declared within classes/methods/field and for types.
 */
public class Annotation extends AbstractAnnotation {
    /**
     * Is this annotation visible at runtime (via reflection api).
     */
    private boolean isRuntimeVisible;

    public boolean isRuntimeVisible() {
        return isRuntimeVisible;
    }

    public void setRuntimeVisible(boolean runtimeVisible) {
        isRuntimeVisible = runtimeVisible;
    }
}
