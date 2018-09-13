package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.AnnotationAnnotationValue;
import me.aki.tactical.core.annotation.BasicAnnotation;

import java.util.Objects;

public abstract class AbstractTypeAnnotation {
    public final static int SORT_CLASS = 0;
    public final static int SORT_FIELD = 1;
    public final static int SORT_METHOD = 2;

    /**
     * What part of the type should be annotated
     * (e.g. a type parameters or the base type of an array)
     */
    private TypePath typePath;

    /**
     * The actual annotation for the type.
     */
    private BasicAnnotation annotation;

    public AbstractTypeAnnotation(TypePath typePath, BasicAnnotation annotation) {
        this.typePath = typePath;
        this.annotation = annotation;
    }

    public TypePath getTypePath() {
        return typePath;
    }

    public void setTypePath(TypePath typePath) {
        this.typePath = typePath;
    }

    public BasicAnnotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(BasicAnnotation annotation) {
        this.annotation = annotation;
    }

    /**
     * Constant that allows to switch on the type.
     */
    public abstract int getSort();

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
