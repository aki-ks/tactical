package me.aki.tactical.core.constant;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.Type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * An instance of "java.lang.invoke.MethodType".
 */
public class MethodTypeConstant implements BootstrapConstant {
    private final Type[] argumentTypes;
    private final Optional<Type> returnType;

    public MethodTypeConstant(Type[] argumentTypes, Optional<Type> returnType) {
        this.argumentTypes = argumentTypes;
        this.returnType = returnType;
    }

    public Type[] getArgumentTypes() {
        return argumentTypes;
    }

    public Optional<Type> getReturnType() {
        return returnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodTypeConstant that = (MethodTypeConstant) o;
        return Arrays.equals(argumentTypes, that.argumentTypes) &&
                Objects.equals(returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(returnType);
        result = 31 * result + Arrays.hashCode(argumentTypes);
        return result;
    }

    @Override
    public String toString() {
        return MethodTypeConstant.class.getSimpleName() + '{' +
                "argumentTypes=" + Arrays.toString(argumentTypes) +
                ", returnType=" + returnType +
                '}';
    }

    @Override
    public ObjectType getType() {
        return new ObjectType(Path.of("java", "lang", "invoke", "MethodType"));
    }
}
