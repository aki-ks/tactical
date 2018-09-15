package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;

/**
 * Pop an array and an index and push the value at that index within the array.
 */
public class ArrayLoadInsn extends AbstractTypeInsn {
    public ArrayLoadInsn(Type type) {
        super(type);
    }

    @Override
    protected boolean isTypeSupported(Type type) {
        return type instanceof RefType ||
                type instanceof IntType || type instanceof LongType ||
                type instanceof FloatType || type instanceof DoubleType;
    }

    @Override
    public int getPushCount() {
        return 1;
    }

    @Override
    public int getPopCount() {
        return 2;
    }
}
