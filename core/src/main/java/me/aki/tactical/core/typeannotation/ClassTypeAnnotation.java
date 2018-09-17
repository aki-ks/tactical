package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.Annotation;

import java.util.Objects;

/**
 * Type annotation in a classfile declaration.
 */
public class ClassTypeAnnotation extends AbstractTypeAnnotation {
    /**
     * What should be annotated (e.g. supertype or a type parameter)
     */
    private TargetType.ClassTargetType targetType;

    public ClassTypeAnnotation(TypePath typePath, Annotation annotation, TargetType.ClassTargetType targetType) {
        super(typePath, annotation);
        this.targetType = targetType;
    }

    public TargetType.ClassTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType.ClassTargetType targetType) {
        this.targetType = targetType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ClassTypeAnnotation that = (ClassTypeAnnotation) o;
        return Objects.equals(targetType, that.targetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), targetType);
    }

    @Override
    public String toString() {
        return ClassTypeAnnotation.class.getSimpleName() + '{' +
                "targetType=" + targetType +
                ", typePath=" + getTypePath() +
                ", annotation=" + getAnnotation() +
                '}';
    }
}
