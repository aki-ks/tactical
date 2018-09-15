package me.aki.tactical.core.constant;

import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.Type;

/**
 * The "null" value.
 */
public class NullConstant implements Constant {
    private final static NullConstant INSTANCE = new NullConstant();

    public static NullConstant getInstance() {
        return INSTANCE;
    }

    private NullConstant() {}

    @Override
    public Type getType() {
        return ObjectType.OBJECT;
    }
}
