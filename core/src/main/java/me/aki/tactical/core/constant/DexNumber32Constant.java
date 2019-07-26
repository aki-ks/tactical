package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.Type;

/**
 * A numeric 32-bit constant as used within dalvik bytecode.
 *
 * The dalvik vm has only this one type for all numeric values.
 */
public class DexNumber32Constant implements DexNumberConstant {
    private int value;

    public DexNumber32Constant(int value) {
        this.value = value;
    }

    public DexNumber32Constant(float value) {
        this.value = Float.floatToRawIntBits(value);
    }

    public int intValue() {
        return value;
    }

    public float floatValue() {
        return Float.intBitsToFloat(value);
    }

    @Override
    public Type getType() {
        // This constant may either be of int or float type
        return null;
    }

    @Override
    public String toString() {
        return DexNumber32Constant.class.getSimpleName() + '{' +
                "intValue()=" + intValue() +
                ", floatValue()=" + floatValue() +
                '}';
    }
}
