package me.aki.tactical.stack.insn;

import me.aki.tactical.stack.invoke.AbstractInstanceInvoke;
import me.aki.tactical.stack.invoke.Invoke;

/**
 * Invoke a method and push the result (unless it's a void).
 */
public class InvokeInsn extends AbstractInstruction {
    private Invoke invoke;

    public InvokeInsn(Invoke invoke) {
        this.invoke = invoke;
    }

    public Invoke getInvoke() {
        return invoke;
    }

    public void setInvoke(Invoke invoke) {
        this.invoke = invoke;
    }

    @Override
    public int getPushCount() {
        return invoke.getMethod().getReturnType().isPresent() ? 1 : 0;
    }

    @Override
    public int getPopCount() {
        int instance = invoke instanceof AbstractInstanceInvoke ? 1 : 0;
        int args = invoke.getMethod().getArguments().size();
        return instance + args;
    }
}
