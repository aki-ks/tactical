package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;

import java.util.Objects;

public class LongConstant implements FieldConstant {
    private final long value;

    public LongConstant(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        me.aki.tactical.core.constant.LongConstant that = (me.aki.tactical.core.constant.LongConstant) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return me.aki.tactical.core.constant.LongConstant.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }

    @Override
    public Type getType() {
        return LongType.getInstance();
    }
}
