package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.Type;

import java.util.Objects;

/**
 * Code shared between instruction that can only operate on certain types.
 */
public abstract class AbstractTypeInsn extends AbstractInstruction {
    /**
     * Types that this instruction may operate on.
     */
    private Type type;

    public AbstractTypeInsn(Type type) {
        setType(type);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        if (!isTypeSupported(type)) {
            throw new IllegalArgumentException(type + "is not supported by this instruction");
        }

        this.type = type;
    }

    protected abstract boolean isTypeSupported(Type type);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractTypeInsn that = (AbstractTypeInsn) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }
}
