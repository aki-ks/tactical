package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.Annotation;

import java.util.Objects;

public abstract class AbstractTypeAnnotation {
    /**
     * What part of the type should be annotated
     * (e.g. a type parameters or the base type of an array)
     */
    private TypePath typePath;

    /**
     * The actual annotation for the type.
     */
    private Annotation annotation;

    public AbstractTypeAnnotation(TypePath typePath, Annotation annotation) {
        this.typePath = typePath;
        this.annotation = annotation;
    }

    public TypePath getTypePath() {
        return typePath;
    }

    public void setTypePath(TypePath typePath) {
        this.typePath = typePath;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTypeAnnotation that = (AbstractTypeAnnotation) o;
        return Objects.equals(typePath, that.typePath) &&
                Objects.equals(annotation, that.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typePath, annotation);
    }
}
