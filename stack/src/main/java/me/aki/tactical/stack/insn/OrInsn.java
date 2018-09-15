package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;

/**
 * Pop two int/long values, calculate the logical or ('|' operator) and push the result.
 */
public class OrInsn extends AbstractBinaryMathInsn {
    public OrInsn(Type type) {
        super(type);
        if (!(type instanceof IntType) && !(type instanceof LongType)) {
            throw new IllegalArgumentException(type + " is not supported by logical or instruction");
        }
    }
}
