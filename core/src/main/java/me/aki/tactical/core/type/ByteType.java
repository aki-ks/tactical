package me.aki.tactical.core.type;

public class ByteType implements IntLikeType {
    private static final ByteType INSTANCE = new ByteType();

    public static ByteType getInstance() {
        return INSTANCE;
    }

    private ByteType() {}

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{}";
    }
}
