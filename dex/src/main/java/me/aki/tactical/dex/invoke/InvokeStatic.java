package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Set;

/**
 * Invoke a static method.
 */
public class InvokeStatic extends ConcreteInvoke {
    public InvokeStatic(MethodRef method, List<Register> arguments) {
        super(method, arguments);
    }

    @Override
    public Set<Register> getRegisterReads() {
        return Set.copyOf(getArguments());
    }

    @Override
    public Set<RWCell<Register>> getRegisterReadCells() {
        return Set.copyOf(getArgumentCells());
    }
}
