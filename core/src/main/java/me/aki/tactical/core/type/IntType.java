package me.aki.tactical.core.type;

public class IntType implements IntLikeType {
    private static final IntType INSTANCE = new IntType();

    public static IntType getInstance() {
        return INSTANCE;
    }

    private IntType() {}

    @Override
    public String toString() {
        return IntType.class.getSimpleName() + "{}";
    }
}
