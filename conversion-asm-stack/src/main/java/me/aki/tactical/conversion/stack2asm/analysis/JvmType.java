package me.aki.tactical.conversion.stack2asm.analysis;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;

public enum JvmType {
    INT, LONG(true), FLOAT, DOUBLE(true), REFERENCE;

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

    /**
     * Does this type required two slots on the operand stack
     */
    private final boolean is64bit;

    JvmType() {
        this(false);
    }

    JvmType(boolean is64bit) {
        this.is64bit = is64bit;
    }

    public boolean is64bit() {
        return is64bit;
    }
}
