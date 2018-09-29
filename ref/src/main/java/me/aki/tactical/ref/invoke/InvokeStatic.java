package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;

public class InvokeStatic extends AbstractConcreteInvoke {
    /**
     * Is the method declared within an interface
     */
    private boolean isInterface;

    public InvokeStatic(MethodRef method, List<Expression> arguments, boolean isInterface) {
        super(method, arguments);
        this.isInterface = isInterface;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.copyOf(getArgumentCells());
    }
}
