package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.Annotation;

import java.util.Objects;

/**
 * Annotated the type of a local variable.
 */
public class LocalVariableTypeAnnotation extends AbstractTypeAnnotation {
    private TargetType.LocalTargetType targetType;

    public LocalVariableTypeAnnotation(TypePath typePath, Annotation annotation, TargetType.LocalTargetType targetType) {
        super(typePath, annotation);
        this.targetType = targetType;
    }

    public TargetType.LocalTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType.LocalTargetType targetType) {
        this.targetType = targetType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LocalVariableTypeAnnotation that = (LocalVariableTypeAnnotation) o;
        return Objects.equals(targetType, that.targetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), targetType);
    }

    @Override
    public String toString() {
        return LocalVariableTypeAnnotation.class.getSimpleName() + '{' +
                "typePath=" + getTypePath() +
                ", annotation=" + getAnnotation() +
                '}';
    }
}
