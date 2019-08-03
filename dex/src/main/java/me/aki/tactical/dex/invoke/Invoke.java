package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public List<RWCell<Register>> getArgumentCells() {
        return IntStream.range(0, arguments.size())
                .mapToObj(index -> RWCell.ofList(arguments, index, Register.class))
                .collect(Collectors.toList());
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

    /**
     * Cells of registers that this invoke reads from.
     *
     * @return all accessed registers
     */
    public abstract List<RWCell<Register>> getRegisterReadCells();
}
