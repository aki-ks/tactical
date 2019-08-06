package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.Type;

/**
 * The "null" value.
 */
public class NullConstant implements PushableConstant, Constant, DexConstant {
    private final static NullConstant INSTANCE = new NullConstant();

    public static NullConstant getInstance() {
        return INSTANCE;
    }

    private NullConstant() {}

    @Override
    public String toString() {
        return NullConstant.class.getSimpleName() + "{}";
    }

    @Override
    public Type getType() {
        return ObjectType.OBJECT;
    }
}
