package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.BooleanType;
import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;

import java.util.Objects;

/**
 * Pop a primitive value from the stack, cast it to another primitive type and push it again.
 *
 * @see RefCastInsn for casts of reference types
 */
public class PrimitiveCastInsn extends AbstractInstruction {
    /**
     * Type of the value before the cast.
     */
    private PrimitiveType fromType;

    /**
     * Type that the value will be casted to.
     */
    private PrimitiveType toType;

    public PrimitiveCastInsn(PrimitiveType fromType, PrimitiveType toType) {
        setFromType(fromType);
        setToType(toType);
    }

    public PrimitiveType getFromType() {
        return fromType;
    }

    public void setFromType(PrimitiveType fromType) {
        if (fromType instanceof IntType || fromType instanceof LongType ||
                fromType instanceof FloatType || fromType instanceof DoubleType) {
            this.fromType = fromType;
        } else {
            throw new IllegalArgumentException("Cast from " + fromType + " is not possible");
        }
    }

    public PrimitiveType getToType() {
        return toType;
    }

    public void setToType(PrimitiveType toType) {
        if (toType instanceof BooleanType) {
            throw new IllegalArgumentException("Cast to BooleanType are not possible");
        }

        this.toType = toType;
    }

    @Override
    public int getPushCount() {
        return 1;
    }

    @Override
    public int getPopCount() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrimitiveCastInsn that = (PrimitiveCastInsn) o;
        return Objects.equals(fromType, that.fromType) &&
                Objects.equals(toType, that.toType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fromType, toType);
    }
}
