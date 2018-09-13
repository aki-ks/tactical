package me.aki.tactical.core.annotation;

import me.aki.tactical.core.Path;

import java.util.LinkedHashMap;

public class AbstractAnnotation {
    /**
     * Class of this annotation.
     */
    private Path type;

    /**
     * Values of all methods of the annotation.
     */
    private LinkedHashMap<String, AnnotationValue> values;

    public Path getType() {
        return type;
    }

    public void setType(Path type) {
        this.type = type;
    }

    public LinkedHashMap<String, AnnotationValue> getValues() {
        return values;
    }

    public void setValues(LinkedHashMap<String, AnnotationValue> values) {
        this.values = values;
    }
}
