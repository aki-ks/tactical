package me.aki.tactical.stack.invoke;

import me.aki.tactical.core.MethodRef;

/**
 * Invoke a static method.
 */
public class StaticInvoke extends AbstractConcreteInvoke {
    /**
     * Is the invoked method declared within an interface
     */
    private boolean isInterface;

    public StaticInvoke(MethodRef method, boolean isInterface) {
        super(method);
        this.isInterface = isInterface;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean isInterface) {
        this.isInterface = isInterface;
    }
}
