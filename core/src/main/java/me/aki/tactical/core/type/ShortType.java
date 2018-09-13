package me.aki.tactical.core.type;

public class ShortType implements IntLikeType {
    private static final ShortType INSTANCE = new ShortType();

    public static ShortType getInstance() {
        return INSTANCE;
    }

    private ShortType() {}

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{}";
    }
}
