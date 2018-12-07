package me.aki.tactical.dex.insn;

import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.invoke.Invoke;

import java.util.List;
import java.util.Optional;

/**
 * Invoke a method. The return value can be retrieved with a {@link MoveResultInstruction}.
 */
public class InvokeInstruction implements Instruction {
    private Invoke invoke;

    public InvokeInstruction(Invoke invoke) {
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
