package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.Annotation;

import java.util.Objects;

/**
 * Annotate a type within a method declaration
 */
public class MethodTypeAnnotation extends AbstractTypeAnnotation {
    /**
     * What should be annotated (e.g. return type or a parameter type)
     */
    private TargetType.MethodTargetType targetType;

    public MethodTypeAnnotation(TypePath typePath, Annotation annotation, TargetType.MethodTargetType targetType) {
        super(typePath, annotation);
        this.targetType = targetType;
    }

    public TargetType.MethodTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType.MethodTargetType targetType) {
        this.targetType = targetType;
    }

    @Override
    public int getSort() {
        return AbstractTypeAnnotation.SORT_METHOD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MethodTypeAnnotation that = (MethodTypeAnnotation) o;
        return Objects.equals(targetType, that.targetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), targetType);
    }

    @Override
    public String toString() {
        return MethodTypeAnnotation.class.getSimpleName() + '{' +
                "targetType=" + targetType +
                ", typePath=" + getTypePath() +
                ", annotation=" + getAnnotation() +
                '}';
    }
}
