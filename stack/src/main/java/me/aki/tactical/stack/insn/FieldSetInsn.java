package me.aki.tactical.stack.insn;

import me.aki.tactical.core.FieldRef;

/**
 * Store a value in a field.
 */
public class FieldSetInsn extends AbstractFieldInsn {
    public FieldSetInsn(FieldRef field, boolean isStatic) {
        super(field, isStatic);
    }

    @Override
    public int getPushCount() {
        return 0;
    }

    @Override
    public int getPopCount() {
        return isStatic() ? 1 : 2;
    }
}
