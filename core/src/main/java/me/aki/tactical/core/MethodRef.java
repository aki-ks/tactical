package me.aki.tactical.core;

import me.aki.tactical.core.type.Type;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MethodRef {
    /**
     * Package and name of class containing the method
     */
    private Path owner;

    /**
     * Name of the Method
     */
    private String name;

    /**
     * Types of the method arguments
     */
    private List<Type> arguments;

    /**
     * Return type of the method or empty for "void".
     */
    private Optional<Type> returnType;

    public MethodRef(Path owner, String name, List<Type> arguments, Optional<Type> returnType) {
        this.owner = owner;
        this.name = name;
        this.arguments = arguments;
        this.returnType = returnType;
    }

    public Path getOwner() {
        return owner;
    }

    public void setOwner(Path owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Type> getArguments() {
        return arguments;
    }

    public void setArguments(List<Type> arguments) {
        this.arguments = arguments;
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
        MethodRef methodRef = (MethodRef) o;
        return Objects.equals(owner, methodRef.owner) &&
                Objects.equals(name, methodRef.name) &&
                Objects.equals(arguments, methodRef.arguments) &&
                Objects.equals(returnType, methodRef.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name, arguments, returnType);
    }

    @Override
    public String toString() {
        return MethodRef.class.getSimpleName() + '{' +
                "owner=" + owner +
                ", name='" + name + '\'' +
                ", arguments=" + arguments +
                ", returnType=" + returnType +
                '}';
    }
}
