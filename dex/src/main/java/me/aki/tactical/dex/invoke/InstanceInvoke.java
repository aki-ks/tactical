package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Invoke a method on an instance of a class
 */
public abstract class InstanceInvoke extends ConcreteInvoke {
    /**
     * Instance of the class containing the method.
     */
    private Register instance;

    public InstanceInvoke(MethodRef method, Register instance, List<Register> arguments) {
        super(method, arguments);
        this.instance = instance;
    }

    public Register getInstance() {
        return instance;
    }

    public void setInstance(Register instance) {
        this.instance = instance;
    }

    public RWCell<Register> getInstanceCell() {
        return RWCell.of(this::getInstance, this::setInstance, Register.class);
    }

    @Override
    public Set<Register> getRegisterReads() {
        return Stream.concat(Stream.of(instance), getArguments().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<RWCell<Register>> getRegisterReadCells() {
        return Stream.concat(Stream.of(getInstanceCell()), getArgumentCells().stream())
                .collect(Collectors.toUnmodifiableSet());
    }
}
