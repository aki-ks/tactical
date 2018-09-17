package me.aki.tactical.stack.insn;

import me.aki.tactical.core.FieldRef;

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
}
