package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.RefType;

import java.util.Objects;

/**
 * Pop a reference type, cast it to another object/array-type and push it again.
 *
 * @see PrimitiveCastInsn for casts between primitive types.
 */
public class RefCastInsn extends AbstractInstruction {
    /**
     * Type that the value should be casted to.
     */
    private RefType type;

    public RefCastInsn(RefType type) {
        this.type = type;
    }

    public RefType getType() {
        return type;
    }

    public void setType(RefType type) {
        this.type = type;
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
        RefCastInsn that = (RefCastInsn) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }
}
