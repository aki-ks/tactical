package me.aki.tactical.stack.insn;

import me.aki.tactical.core.constant.PushableConstant;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PushInsn pushInsn = (PushInsn) o;
        return Objects.equals(constant, pushInsn.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), constant);
    }
}
