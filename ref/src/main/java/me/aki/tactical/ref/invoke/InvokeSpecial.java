package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.ref.Expression;

import java.util.List;

public class InvokeSpecial extends AbstractInstanceInvoke {
    /**
     * Is the method declared within an interface.
     */
    private boolean isInterface;

    public InvokeSpecial(MethodRef method, Expression instance, List<Expression> arguments, boolean isInterface) {
        super(method, instance, arguments);
        this.isInterface = isInterface;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }
}
