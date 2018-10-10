package me.aki.tactical.core;

import me.aki.tactical.core.type.Type;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Argument types and return type of a method.
 */
public class MethodDescriptor {
    /**
     * Types of method parameters.
     */
    private List<Type> parameterTypes;

    /**
     * Type of value returned by the method or empty for "void".
     */
    private Optional<Type> returnType;

    public MethodDescriptor(List<Type> parameterTypes, Optional<Type> returnType) {
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDescriptor that = (MethodDescriptor) o;
        return Objects.equals(parameterTypes, that.parameterTypes) &&
                Objects.equals(returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterTypes, returnType);
    }

    @Override
    public String toString() {
        return MethodDescriptor.class.getSimpleName() + '{' +
                "parameterTypes=" + parameterTypes +
                ", returnType=" + returnType +
                '}';
    }
}
