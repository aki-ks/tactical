package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.Annotation;

/**
 * Annotated the type of a local variable.
 */
public class LocalVariableTypeAnnotation extends AbstractTypeAnnotation {
    public LocalVariableTypeAnnotation(TypePath typePath, Annotation annotation) {
        super(typePath, annotation);
    }

    @Override
    public String toString() {
        return LocalVariableTypeAnnotation.class.getSimpleName() + '{' +
                "typePath=" + getTypePath() +
                ", annotation=" + getAnnotation() +
                '}';
    }
}
