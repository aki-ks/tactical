package me.aki.tactical.core;

import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.annotation.AnnotationValue;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A method definition within a {@link Classfile}
 */
public class Method {
    /**
     * All access flags that are set for this method.
     */
    private Set<Flag> flags = new HashSet<>();

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
     * Checked exceptions that might be thrown by this method.
     */
    private List<Path> exceptions = new ArrayList<>();

    /**
     * Signature of the method with type variables.
     */
    private Optional<String> signature = Optional.empty();

    /**
     * Default value for methods in interfaces.
     */
    private Optional<AnnotationValue> defaultValue = Optional.empty();

    /**
     * Additional debug informations about parameters.
     *
     * It will only be added to a classfile if javac is run with the "-parameters" flag.
     * The information is runtime accessible via the java reflection api.
     */
    private List<Parameter> parameterInfo = new ArrayList<>();

    /**
     * A list of parameter annotations.
     */
    private List<List<Annotation>> parameterAnnotations = new ArrayList<>();

    /**
     * Annotations of this methods.
     */
    private List<Annotation> annotations = new ArrayList<>();

    /**
     * Annotations on types within this method.
     */
    private List<MethodTypeAnnotation> typeAnnotations = new ArrayList<>();

    /**
     * Non-parsed attributes of this method.
     *
     * They are either not part of the JVM spec or
     * are not yet supported by this library.
     */
    private List<Attribute> attributes = new ArrayList<>();

    /**
     * Body of the method that contains the instructions.
     * It is absent for abstract methods.
     */
    private Optional<Body> body = Optional.empty();

    public Method(String name, List<Type> parameterTypes, Optional<Type> returnType) {
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    public Set<Flag> getFlags() {
        return flags;
    }

    public void setFlags(Set<Flag> flags) {
        this.flags = flags;
    }

    /**
     * Check whether a {@link Flag} is set for this field.
     *
     * @param flag flag to check for
     * @return is the flag set
     */
    public boolean getFlag(Flag flag) {
        return flags.contains(flag);
    }

    /**
     * Set/remove a {@link Flag}.
     *
     * @param flag to be set/removed
     * @param shouldSet should the flag be set or removed
     */
    public void setFlag(Flag flag, boolean shouldSet) {
        if (shouldSet) {
            flags.add(flag);
        } else {
            flags.remove(flag);
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

    public List<Path> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<Path> exceptions) {
        this.exceptions = exceptions;
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

    public List<Parameter> getParameterInfo() {
        return parameterInfo;
    }

    public void setParameterInfo(List<Parameter> parameterInfo) {
        this.parameterInfo = parameterInfo;
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

    public Optional<Body> getBody() {
        return body;
    }

    public void setBody(Optional<Body> body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Method method = (Method) o;
        return Objects.equals(flags, method.flags) &&
                Objects.equals(name, method.name) &&
                Objects.equals(parameterTypes, method.parameterTypes) &&
                Objects.equals(returnType, method.returnType) &&
                Objects.equals(exceptions, method.exceptions) &&
                Objects.equals(signature, method.signature) &&
                Objects.equals(defaultValue, method.defaultValue) &&
                Objects.equals(parameterInfo, method.parameterInfo) &&
                Objects.equals(parameterAnnotations, method.parameterAnnotations) &&
                Objects.equals(annotations, method.annotations) &&
                Objects.equals(typeAnnotations, method.typeAnnotations) &&
                Objects.equals(attributes, method.attributes) &&
                Objects.equals(body, method.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags, name, parameterTypes, returnType, exceptions, signature,
                defaultValue, parameterInfo, parameterAnnotations, annotations, typeAnnotations, attributes, body);
    }

    @Override
    public String toString() {
        return Method.class.getSimpleName() + '{' +
                "flags=" + flags +
                ", name='" + name + '\'' +
                ", parameterTypes=" + parameterTypes +
                ", returnType=" + returnType +
                ", exceptions=" + exceptions +
                ", signature=" + signature +
                ", defaultValue=" + defaultValue +
                ", parameterInfo=" + parameterInfo +
                ", parameterAnnotations=" + parameterAnnotations +
                ", annotations=" + annotations +
                ", typeAnnotations=" + typeAnnotations +
                ", attributes=" + attributes +
                ", body=" + body +
                '}';
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Parameter parameter = (Parameter) o;
            return Objects.equals(name, parameter.name) &&
                    Objects.equals(flags, parameter.flags);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, flags);
        }

        @Override
        public String toString() {
            return Parameter.class.getSimpleName() + '{' +
                    "name=" + name +
                    ", flags=" + flags +
                    '}';
        }

        public static enum Flag {
            FINAL, SYNTHETIC, MANDATED
        }
    }
}
