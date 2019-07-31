package me.aki.tactical.dex;

/**
 * A dex type that differentiates between the actual int type and
 * other int-like (boolean, byte, short, char) types.
 */
public enum DetailedDexType {
    BOOLEAN(DexType.NORMAL),
    BYTE(DexType.NORMAL),
    SHORT(DexType.NORMAL),
    CHAR(DexType.NORMAL),

    NORMAL(DexType.NORMAL), // int or float
    WIDE(DexType.WIDE), // long or double
    OBJECT(DexType.OBJECT);

    private final DexType dexType;

    DetailedDexType(DexType dexType) {
        this.dexType = dexType;
    }

    public DexType toDexType() {
        return dexType;
    }
}
