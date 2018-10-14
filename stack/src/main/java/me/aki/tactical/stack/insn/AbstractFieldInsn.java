package me.aki.tactical.stack.insn;

import me.aki.tactical.core.FieldRef;

import java.util.Objects;

/**
 * Instruction that operates on a field
 */
public abstract class AbstractFieldInsn extends AbstractInstruction {
    /**
     * Field that this instruction operates on
     */
    private FieldRef field;

    /**
     * Is the field static
     */
    private boolean isStatic;

    public AbstractFieldInsn(FieldRef field, boolean isStatic) {
        this.field = field;
        this.isStatic = isStatic;
    }

    public FieldRef getField() {
        return field;
    }

    public void setField(FieldRef field) {
        this.field = field;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractFieldInsn fieldInsn = (AbstractFieldInsn) o;
        return isStatic == fieldInsn.isStatic &&
                Objects.equals(field, fieldInsn.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), field, isStatic);
    }
}
