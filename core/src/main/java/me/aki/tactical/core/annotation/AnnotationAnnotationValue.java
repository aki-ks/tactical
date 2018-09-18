package me.aki.tactical.core.annotation;

import me.aki.tactical.core.Path;

/**
 * An annotation used as value of a method in a method declaration
 */
public class AnnotationAnnotationValue extends AbstractAnnotation implements AnnotationValue {
    public AnnotationAnnotationValue(Path type) {
        super(type);
    }
}
