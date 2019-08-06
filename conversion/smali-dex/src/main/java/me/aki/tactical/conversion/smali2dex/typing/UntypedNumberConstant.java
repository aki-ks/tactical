package me.aki.tactical.conversion.smali2dex.typing;

import me.aki.tactical.core.constant.DexConstant;
import me.aki.tactical.core.type.Type;

/**
 * A numeric constant whose type is not known.
 * Its value can be interpreted as int, long, float or double.
 *
 * This constant is temporary created during conversion.
 */
public class UntypedNumberConstant implements DexConstant {
    private final long value;

    public UntypedNumberConstant(int value) {
        this.value = value;
    }

    public UntypedNumberConstant(long value) {
        this.value = value;
    }

    public UntypedNumberConstant(float value) {
        this.value = Float.floatToRawIntBits(value);
    }

    public UntypedNumberConstant(double value) {
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
    public Type getType() {
        // Is not statically known
        return null;
    }
}
