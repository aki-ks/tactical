package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.Type;

/**
 * A numeric 64-bit constant as used within dalvik bytecode.
 *
 * The dalvik vm has only this one type for all numeric values.
 */
public class DexNumber64Constant implements DexNumberConstant {
    private long value;

    public DexNumber64Constant(long value) {
        this.value = value;
    }

    public DexNumber64Constant(double value) {
        this.value = Double.doubleToLongBits(value);
    }

    public long longValue() {
        return value;
    }

    public double doubleValue() {
        return Double.longBitsToDouble(value);
    }

    @Override
    public Type getType() {
        // This constant may either be of long or double type
        return null;
    }

    @Override
    public String toString() {
        return DexNumber64Constant.class.getSimpleName() + '{' +
                ", longValue()=" + longValue() +
                ", doubleValue()=" + doubleValue() +
                '}';
    }
}
