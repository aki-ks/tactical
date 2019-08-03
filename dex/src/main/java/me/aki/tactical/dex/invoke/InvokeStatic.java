package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;

/**
 * Invoke a static method.
 */
public class InvokeStatic extends ConcreteInvoke {
    public InvokeStatic(MethodRef method, List<Register> arguments) {
        super(method, arguments);
    }

    @Override
    public List<Register> getRegisterReads() {
        return List.copyOf(getArguments());
    }

    @Override
    public List<RWCell<Register>> getRegisterReadCells() {
        return getArgumentCells();
    }
}
