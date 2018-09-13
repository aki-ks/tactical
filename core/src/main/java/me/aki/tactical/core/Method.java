package me.aki.tactical.core;

import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.annotation.AnnotationValue;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A method definition within a {@link Classfile}
 */
public class Method {
    /**
     * All access flags that are set for this method.
     */
    private Set<Flag> accessFlags;

    /**
     * Name of the method
     */
    private String name;

    /**
     * Types of the parameters
     */
    private List<Type> parameterTypes;

    /**
     * Type returned by the method or empty for void.
     */
    private Optional<Type> returnType;

    /**
     * Signature of the method with type variables.
     */
    private Optional<String> signature;

    /**
     * Default value for methods in interfaces.
     */
    private Optional<AnnotationValue> defaultValue;

    /**
     * A list of parameter annotations.
     */
    private List<List<Annotation>> parameterAnnotations;

    /**
     * Annotations of this methods.
     */
    private List<Annotation> annotations;

    /**
     * Annotations on types within this method.
     */
    private List<MethodTypeAnnotation> typeAnnotations;

    /**
     * Non-parsed attributes of this method.
     *
     * They are either not part of the JVM spec or
     * are not yet supported by this library.
     */
    private List<Attribute> attributes;

    public Method(Set<Flag> accessFlags, String name, List<Type> parameterTypes,
                  Optional<Type> returnType, List<Annotation> annotations,
                  List<MethodTypeAnnotation> typeAnnotations, List<Attribute> attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.annotations = annotations;
        this.typeAnnotations = typeAnnotations;
        this.attributes = attributes;
    }

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

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<Type> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Optional<Type> getReturnType() {
        return returnType;
    }

    public void setReturnType(Optional<Type> returnType) {
        this.returnType = returnType;
    }

    public boolean isVoid() {
        return !returnType.isPresent();
    }

    public Optional<String> getSignature() {
        return signature;
    }

    public void setSignature(Optional<String> signature) {
        this.signature = signature;
    }

    public Optional<AnnotationValue> getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Optional<AnnotationValue> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<List<Annotation>> getParameterAnnotations() {
        return parameterAnnotations;
    }

    public void setParameterAnnotations(List<List<Annotation>> parameterAnnotations) {
        this.parameterAnnotations = parameterAnnotations;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public List<MethodTypeAnnotation> getTypeAnnotations() {
        return typeAnnotations;
    }

    public void setTypeAnnotations(List<MethodTypeAnnotation> typeAnnotations) {
        this.typeAnnotations = typeAnnotations;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public static enum Flag {
        PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, SYNCHRONIZED, BRIDGE, VARARGS, NATIVE, ABSTRACT, STRICT, SYNTHETIC
    }

    public static class Parameter {
        /**
         * Name of the parameter
         */
        private Optional<String> name;

        /**
         * Access flags of the parameter
         */
        private Set<Flag> flags;

        public Parameter() {
            this(Optional.empty(), new HashSet<>());
        }

        public Parameter(Optional<String> name, Set<Flag> flags) {
            this.name = name;
            this.flags = flags;
        }

        public Optional<String> getName() {
            return name;
        }

        public void setName(Optional<String> name) {
            this.name = name;
        }

        public Set<Flag> getFlags() {
            return flags;
        }

        public void setFlags(Set<Flag> flags) {
            this.flags = flags;
        }

        public static enum Flag {
            FINAL, SYNTHETIC, MANDATED
        }
    }
}
