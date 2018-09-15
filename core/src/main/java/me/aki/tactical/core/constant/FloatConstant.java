package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.Type;

import java.util.Objects;

public class FloatConstant implements FieldConstant {
    private final float value;

    public FloatConstant(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        me.aki.tactical.core.constant.FloatConstant that = (me.aki.tactical.core.constant.FloatConstant) o;
        return Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return me.aki.tactical.core.constant.FloatConstant.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }

    @Override
    public Type getType() {
        return FloatType.getInstance();
    }
}
