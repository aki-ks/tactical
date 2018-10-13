package me.aki.tactical.dex;

/**
 * A dex type that differentiates between the actual int type and
 * other int-like (boolean, byte, short, char) types.
 */
public enum DetailedDexType {
    BOOLEAN,
    BYTE,
    SHORT,
    CHAR,

    NORMAL, // int or float
    WIDE, // long or double
    OBJECT;
}
