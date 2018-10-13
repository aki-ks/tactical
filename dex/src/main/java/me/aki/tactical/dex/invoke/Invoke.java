package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.dex.Register;

import java.util.List;

public abstract class Invoke {
    /**
     * Registers that contain the argument values.
     */
    private List<Register> arguments;

    public Invoke(List<Register> arguments) {
        this.arguments = arguments;
    }

    public List<Register> getArguments() {
        return arguments;
    }

    public void setArguments(List<Register> arguments) {
        this.arguments = arguments;
    }

    /**
     * Get the parameter and return type of the method to be invoked.
     *
     * @return descriptor of the invoked method
     */
    public abstract MethodDescriptor getDescriptor();

    /**
     * Registers that this invoke reads from.
     *
     * @return all accessed registers
     */
    public abstract List<Register> getRegisterReads();
}
