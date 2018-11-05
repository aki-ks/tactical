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
 * Push a value from a local onto the stack.
 */
public class LoadInsn extends AbstractTypeInsn {
    /**
     * Local whose value will be pushed onto the stack
     */
    private StackLocal local;

    public LoadInsn(Type type, StackLocal local) {
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
        return 1;
    }

    @Override
    public int getPopCount() {
        return 0;
    }
}
