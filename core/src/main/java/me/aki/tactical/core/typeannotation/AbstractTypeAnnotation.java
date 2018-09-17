package me.aki.tactical.core.typeannotation;

import me.aki.tactical.core.annotation.Annotation;

import java.util.Objects;

public abstract class AbstractTypeAnnotation {
    public final static int SORT_CLASS = 0;
    public final static int SORT_FIELD = 1;
    public final static int SORT_METHOD = 2;
    public final static int SORT_INSN = 3;
    public final static int SORT_EXCEPTION = 4;

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
