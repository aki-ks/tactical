package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.Objects;

public class InvokeSpecial extends AbstractInstanceInvoke implements AmbigiousInvoke {
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

    public void setInterface(boolean isInterface) {
        this.isInterface = isInterface;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InvokeSpecial that = (InvokeSpecial) o;
        return isInterface == that.isInterface;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isInterface);
    }
}
