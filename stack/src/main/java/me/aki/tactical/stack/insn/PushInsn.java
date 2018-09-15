package me.aki.tactical.stack.insn;

import me.aki.tactical.core.constant.Constant;

/**
 * Push a constant value onto the stack.
 */
public class PushInsn implements Instruction {
    private Constant constant;

    public PushInsn(Constant constant) {
        this.constant = constant;
    }

    public Constant getConstant() {
        return constant;
    }

    public void setConstant(Constant constant) {
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
