package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.stack.StackLocal;

import java.util.Objects;

/**
 * Store the upper value of the stack in a local.
 */
public class StoreInsn extends AbstractTypeInsn {
    private StackLocal local;

    public StoreInsn(Type type, StackLocal local) {
        super(type);
        this.local = local;
    }

    @Override
    protected boolean isTypeSupported(Type type) {
        return type instanceof RefType ||
                type instanceof IntType || type instanceof LongType ||
                type instanceof FloatType || type instanceof DoubleType;
    }

    public StackLocal getLocal() {
        return local;
    }

    public void setLocal(StackLocal local) {
        this.local = local;
    }

    @Override
    public int getPushCount() {
        return 0;
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
        StoreInsn storeInsn = (StoreInsn) o;
        return Objects.equals(local, storeInsn.local);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), local);
    }
}
