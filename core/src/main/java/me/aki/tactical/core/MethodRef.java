package me.aki.tactical.core;

import me.aki.tactical.core.type.Type;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Uniquely identify a method in a class.
 */
public class MethodRef {
    /**
     * Package and name of class containing the method
     */
    private final Path owner;

    /**
     * Name of the Method
     */
    private final String name;

    /**
     * Types of the method arguments
     */
    private final List<Type> arguments;

    /**
     * Return type of the method or empty for "void".
     */
    private final Optional<Type> returnType;

    public MethodRef(Path owner, String name, List<Type> arguments, Optional<Type> returnType) {
        this.owner = owner;
        this.name = name;
        this.arguments = List.copyOf(arguments);
        this.returnType = returnType;
    }

    public Path getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public List<Type> getArguments() {
        return arguments;
    }

    public Optional<Type> getReturnType() {
        return returnType;
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
