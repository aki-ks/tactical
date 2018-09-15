package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.Type;

/**
 * Pop two values of type 'long' from the stack,
 * compare them and push a result of int type.
 *
 * If both numbers are equal push 0.
 * Otherwise either 1 or -1 is pushed,
 * depending on which number is greater.
 */
public class CmpInsn extends AbstractBinaryMathInsn implements Instruction {
    public CmpInsn() {
        super(LongType.getInstance());
    }

    @Override
    protected boolean isTypeSupported(Type type) {
        return type instanceof LongType;
    }
}
