package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.invoke.Invoke;

import java.util.List;
import java.util.Optional;

/**
 * Invoke a method. The return value can be retrieved with a {@link MoveResultStatement}.
 */
public class InvokeStatement implements Statement {
    private Invoke invoke;

    public InvokeStatement(Invoke invoke) {
        this.invoke = invoke;
    }

    public Invoke getInvoke() {
        return invoke;
    }

    public void setInvoke(Invoke invoke) {
        this.invoke = invoke;
    }

    @Override
    public List<Register> getReadRegisters() {
        return invoke.getRegisterReads();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }
}
