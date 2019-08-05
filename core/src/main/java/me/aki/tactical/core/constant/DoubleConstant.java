package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.Type;

import java.util.Objects;

public class DoubleConstant implements PrimitiveValueConstant {
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
        DoubleConstant that = (DoubleConstant) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return DoubleConstant.class.getSimpleName() + '{' +
                "value=" + value +
                '}';
    }

    @Override
    public Type getType() {
        return DoubleType.getInstance();
    }
}
