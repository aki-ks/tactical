package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.Annotation;

/**
 * Annotate the exception type of a try/catch block.
 */
public class ExceptionTypeAnnotation extends AbstractTypeAnnotation {
    public ExceptionTypeAnnotation(TypePath typePath, Annotation annotation) {
        super(typePath, annotation);
    }

    @Override
    public int getSort() {
        return SORT_EXCEPTION;
    }

    @Override
    public String toString() {
        return FieldTypeAnnotation.class.getSimpleName() + '{' +
                "typePath=" + getTypePath() +
                ", annotation=" + getAnnotation() +
                '}';
    }
}
