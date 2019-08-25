package me.aki.tactical.dex.insn;

import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.invoke.Invoke;

import java.util.Optional;
import java.util.Set;

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
    public Set<Register> getReadRegisters() {
        return invoke.getRegisterReads();
    }

    @Override
    public Set<RWCell<Register>> getReadRegisterCells() {
        return invoke.getRegisterReadCells();
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }

    @Override
    public Optional<RWCell<Register>> getWrittenRegisterCell() {
        return Optional.empty();
    }
}
