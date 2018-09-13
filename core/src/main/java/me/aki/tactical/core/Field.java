package me.aki.tactical.core;

import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.FieldTypeAnnotation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A field definition within a {@link Classfile}.
 */
public class Field {
    /**
     * Access flags of the field
     */
    private Set<Flag> accessFlags;

    /**
     * Name of the field
     */
    private String name;

    /**
     * Type of values that can be stored in the field.
     */
    private Type type;

    /**
     * Type of the field with type variables.
     */
    private Optional<String> signature;

    /**
     * Annotations of this field.
     */
    private List<Annotation> annotations;

    private List<FieldTypeAnnotation> typeAnnotations;

    public Field(Set<Flag> accessFlags, String name, Type type, List<Annotation> annotations,
                 List<FieldTypeAnnotation> typeAnnotations, List<Attribute> attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.type = type;
        this.annotations = annotations;
        this.typeAnnotations = typeAnnotations;
        this.attributes = attributes;
    }

    /**
     * Non-parsed attributes of this field.
     *
     * They are either not part of the JVM spec or
     * are not yet supported by this library.
     */
    private List<Attribute> attributes;

    public Set<Flag> getAccessFlags() {
        return accessFlags;
    }

    public void setAccessFlags(Set<Flag> accessFlags) {
        this.accessFlags = accessFlags;
    }

    /**
     * Check whether a {@link Flag} is set for this field.
     *
     * @param flag flag to check for
     * @return is the flag set
     */
    public boolean getFlag(Flag flag) {
        return accessFlags.contains(flag);
    }

    /**
     * Set/remove a {@link Flag}.
     *
     * @param flag to be set/removed
     * @param shouldSet should the flag be set or removed
     */
    public void setFlag(Flag flag, boolean shouldSet) {
        if (shouldSet) {
            accessFlags.add(flag);
        } else {
            accessFlags.remove(flag);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Optional<String> getSignature() {
        return signature;
    }

    public void setSignature(Optional<String> signature) {
        this.signature = signature;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public List<FieldTypeAnnotation> getTypeAnnotations() {
        return typeAnnotations;
    }

    public void setTypeAnnotations(List<FieldTypeAnnotation> typeAnnotations) {
        this.typeAnnotations = typeAnnotations;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public static enum Flag {
        PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, VOLATILE, TRANSIENT, SYNTHETIC, ENUM
    }
}
