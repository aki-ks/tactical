package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.PrimitiveType;

/**
 * A numeric constant as used within dalvik bytecode.
 *
 * The dalvik vm has only this one type for all numeric values.
 */
public class DexNumberConstant implements DexConstant, Constant {
    private long value;

    public DexNumberConstant(int value) {
        this.value = value;
    }

    public DexNumberConstant(long value) {
        this.value = value;
    }

    public DexNumberConstant(float value) {
        this.value = Float.floatToRawIntBits(value);
    }

    public DexNumberConstant(double value) {
        this.value = Double.doubleToLongBits(value);
    }

    public int intValue() {
        return (int) value;
    }

    public long longValue() {
        return value;
    }

    public float floatValue() {
        return Float.intBitsToFloat((int) value);
    }

    public double doubleValue() {
        return Double.longBitsToDouble(value);
    }

    @Override
    public PrimitiveType getType() {
        // We just know that this is some kind of PrimitiveType.
        return null;
    }

    @Override
    public String toString() {
        return DexNumberConstant.class.getSimpleName() + '{' +
                "intValue()=" + intValue() +
                ", longValue()=" + longValue() +
                ", floatValue()=" + floatValue() +
                ", doubleValue()=" + doubleValue() +
                '}';
    }
}
