package me.aki.tactical.stack.insn;

import me.aki.tactical.core.constant.PushableConstant;

/**
 * Push a constant value onto the stack.
 */
public class PushInsn extends AbstractInstruction {
    private PushableConstant constant;

    public PushInsn(PushableConstant constant) {
        this.constant = constant;
    }

    public PushableConstant getConstant() {
        return constant;
    }

    public void setConstant(PushableConstant constant) {
        this.constant = constant;
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
