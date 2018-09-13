package me.aki.tactical.core.type;

public class LongType implements PrimitiveType {
    private static final LongType INSTANCE = new LongType();

    public static LongType getInstance() {
        return INSTANCE;
    }

    private LongType() {}

    @Override
    public String toString() {
        return LongType.class.getSimpleName() + "{}";
    }
}
