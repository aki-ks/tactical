package me.aki.tactical.conversion.stackasm.analysis;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;

public enum JvmType {
    INT(IntType.getInstance()),
    LONG(LongType.getInstance(), true),
    FLOAT(FloatType.getInstance()),
    DOUBLE(DoubleType.getInstance(), true),
    REFERENCE(ObjectType.OBJECT);

    /**
     * Get the corresponding JVM type from a type of the tactical type system.
     *
     * @param type from tactical type system
     * @return corresponding JVM type
     */
    public static JvmType from(Type type) {
        if (type instanceof IntLikeType) {
            return INT;
        } else if (type instanceof LongType) {
            return LONG;
        } else if (type instanceof FloatType) {
            return FLOAT;
        } else if (type instanceof DoubleType) {
            return DOUBLE;
        } else if (type instanceof RefType) {
            return REFERENCE;
        } else {
            throw new AssertionError();
        }
    }

    private final Type type;

    /**
     * Does this type required two slots on the operand stack
     */
    private final boolean is64bit;

    JvmType(Type type) {
        this(type, false);
    }

    JvmType(Type type, boolean is64bit) {
        this.type = type;
        this.is64bit = is64bit;
    }

    public boolean is64bit() {
        return is64bit;
    }

    public boolean is32bit() {
        return !is64bit;
    }

    public Type toType() {
        return type;
    }
}
