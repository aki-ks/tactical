package me.aki.tactical.conversion.smali2dex.typing;

import me.aki.tactical.core.constant.DexConstant;

/**
 * A numeric constant whose type is not known.
 * Its value can be interpreted as int/long/null or float/double.
 *
 * This constant is temporary created during conversion and
 * gets resolved by the {@link DexTyper}.
 */
public class UntypedNumberConstant implements DexConstant {
    private final AmbiguousType type;
    private final long value;

    public UntypedNumberConstant(AmbiguousType type, int value) {
        this.type = type;
        this.value = value;
    }

    public UntypedNumberConstant(AmbiguousType type, long value) {
        this.type = type;
        this.value = value;
    }

    public UntypedNumberConstant(AmbiguousType type, float value) {
        this.type = type;
        this.value = Float.floatToRawIntBits(value);
    }

    public UntypedNumberConstant(AmbiguousType type, double value) {
        this.type = type;
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
    public AmbiguousType getType() {
        return type;
    }

    @Override
    public String toString() {
        return UntypedNumberConstant.class.getSimpleName() + '{' +
                "intValue()=" + intValue() +
                ", longValue()=" + longValue() +
                ", floatValue()=" + floatValue() +
                ", doubleValue()=" + doubleValue() +
                '}';
    }
}
