package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.RefType;

import java.util.Objects;

/**
 * An instance of "java.lang.Class".
 */
public class ClassConstant implements BootstrapConstant, PushableConstant, Constant {
    private final RefType value;

    public ClassConstant(RefType value) {
        this.value = value;
    }

    public RefType getValue() {
        return value;
    }

    public ObjectType getType() {
        return ObjectType.CLASS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassConstant that = (ClassConstant) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return ClassConstant.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }
}
