package me.aki.tactical.core;

import me.aki.tactical.core.type.Type;

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
     * Non-parsed attributes of this method.
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

    public static enum Flag {
        PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, SYNCHRONIZED, BRIDGE, VARARGS, NATIVE, ABSTRACT, STRICT, SYNTHETIC
    }
}
