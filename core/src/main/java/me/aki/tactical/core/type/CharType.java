package me.aki.tactical.core.type;

public class CharType implements IntLikeType {
    private static final CharType INSTANCE = new CharType();

    public static CharType getInstance() {
        return INSTANCE;
    }

    private CharType() {}

    @Override
    public String toString() {
        return CharType.class.getSimpleName() + "{}";
    }
}
