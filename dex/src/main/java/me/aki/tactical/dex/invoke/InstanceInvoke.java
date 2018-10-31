package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.stream.Collectors;
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

    @Override
    public List<Register> getRegisterReads() {
        return Stream.concat(Stream.of(instance), getArguments().stream())
                .collect(Collectors.toUnmodifiableList());
    }
}