package me.aki.tactical.core.constant;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.Type;

import java.util.Optional;

/**
 * An instance of "java.lang.invoke.MethodType".
 */
public class MethodTypeConstant implements Constant {
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
    public ObjectType getType() {
        return new ObjectType(Path.of("java", "lang", "invoke", "MethodType"));
    }
}
