package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.Annotation;

import java.util.Objects;

/**
 * Annotate types within instructions
 */
public class InsnTypeAnnotation extends AbstractTypeAnnotation {
    private TargetType.InsnTargetType targetType;

    public InsnTypeAnnotation(TypePath typePath, Annotation annotation, TargetType.InsnTargetType targetType) {
        super(typePath, annotation);
        this.targetType = targetType;
    }

    public TargetType.InsnTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType.InsnTargetType targetType) {
        this.targetType = targetType;
    }

    @Override
    public int getSort() {
        return SORT_INSN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InsnTypeAnnotation that = (InsnTypeAnnotation) o;
        return Objects.equals(targetType, that.targetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), targetType);
    }

    @Override
    public String toString() {
        return InsnTypeAnnotation.class.getSimpleName() + '{' +
                "targetType=" + targetType +
                ", typePath=" + getTypePath() +
                ", annotation=" + getAnnotation() +
                '}';
    }
}
