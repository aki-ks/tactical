package me.aki.tactical.core.type;

import java.util.Objects;

public class ArrayType implements RefType {
    private final int dimensions;
    private final Type baseType;

    public ArrayType(Type baseType, int dimensions) {
        if (dimensions < 1) {
            throw new IllegalArgumentException("dimension < 1");
        }

        while (baseType instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) baseType;
            dimensions += arrayType.dimensions;
            baseType = arrayType.baseType;
        }

        this.dimensions = dimensions;
        this.baseType = baseType;
    }

    public int getDimensions() {
        return dimensions;
    }

    public Type getBaseType() {
        return baseType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return dimensions == arrayType.dimensions &&
                Objects.equals(baseType, arrayType.baseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimensions, baseType);
    }

    @Override
    public String toString() {
        return ArrayType.class.getSimpleName() + '{' +
                "dimensions=" + dimensions +
                ", baseType=" + baseType +
                '}';
    }
}
