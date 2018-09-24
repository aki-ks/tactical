package me.aki.tactical.stack.insn;

import me.aki.tactical.core.type.RefType;

/**
 * Pop a value from the stack and check whether it is an instance of a certain class / array-type.
 * The result, either 1 (true) or 0 (false) will be pushed onto the stack.
 */
public class InstanceOfInsn extends AbstractInstruction {
    /**
     * Check whether the popped object is an instance of this class.
     */
    private RefType type;

    public InstanceOfInsn(RefType type) {
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
}
