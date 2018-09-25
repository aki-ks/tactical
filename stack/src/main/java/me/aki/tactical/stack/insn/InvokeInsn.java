package me.aki.tactical.stack.insn;

import me.aki.tactical.core.InvokableMethodRef;

/**
 * Invoke a method and push the result (unless it's a void).
 */
public class InvokeInsn extends AbstractInstruction {
    /**
     * How should the method be invoked.
     */
    private InvokeType invokeType;

    /**
     * Method that will be invoked.
     */
    private InvokableMethodRef method;

    public InvokeInsn(InvokeType invokeType, InvokableMethodRef method) {
        this.invokeType = invokeType;
        this.method = method;
    }

    public InvokeType getInvokeType() {
        return invokeType;
    }

    public void setInvokeType(InvokeType invokeType) {
        this.invokeType = invokeType;
    }

    public InvokableMethodRef getMethod() {
        return method;
    }

    public void setMethod(InvokableMethodRef method) {
        this.method = method;
    }

    @Override
    public int getPushCount() {
        boolean isVoid = !method.getReturnType().isPresent();
        return isVoid ? 0 : 1;
    }

    @Override
    public int getPopCount() {
        int instance = invokeType == InvokeType.STATIC ? 0 : 1;
        int args = method.getArguments().size();
        return instance + args;
    }

    public static enum InvokeType {
        /**
         * Invoke a method on a class instance.
         *
         * For some cases {@link InvokeType#SPECIAL} must be used instead.
         */
        VIRTUAL,

        /**
         * Invoke a private method, a method in a superclass or a constructor.
         *
         * These methods have in common that they cannot be overridden.
         */
        SPECIAL,

        /**
         * Invoke a method declared in an interface.
         */
        INTERFACE,

        /**
         * Invoke a static method.
         *
         * No instance will be popped from the stack.
         */
        STATIC
    }
}
