package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.BasicAnnotation;

/**
 * Annotate the type of a field.
 */
public class FieldTypeAnnotation extends AbstractTypeAnnotation {
    public FieldTypeAnnotation(TypePath typePath, BasicAnnotation annotation) {
        super(typePath, annotation);
    }

    @Override
    public int getSort() {
        return AbstractTypeAnnotation.SORT_FIELD;
    }

    @Override
    public String toString() {
        return FieldTypeAnnotation.class.getSimpleName() + '{' +
                "typePath=" + getTypePath() +
                ", annotation=" + getAnnotation() +
                '}';
    }
}
