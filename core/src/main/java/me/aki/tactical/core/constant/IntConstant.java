package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.Type;

import java.util.Objects;

public class IntConstant implements FieldConstant, BootstrapConstant, PushableConstant, Constant {
    private final int value;

    public IntConstant(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntConstant that = (IntConstant) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return IntConstant.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }

    @Override
    public Type getType() {
        return IntType.getInstance();
    }
}
