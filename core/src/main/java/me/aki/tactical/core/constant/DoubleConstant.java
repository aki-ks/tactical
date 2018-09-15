package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.Type;

import java.util.Objects;

public class DoubleConstant implements FieldConstant {
    private final double value;

    public DoubleConstant(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        me.aki.tactical.core.constant.DoubleConstant that = (me.aki.tactical.core.constant.DoubleConstant) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return me.aki.tactical.core.constant.DoubleConstant.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }

    @Override
    public Type getType() {
        return DoubleType.getInstance();
    }
}
