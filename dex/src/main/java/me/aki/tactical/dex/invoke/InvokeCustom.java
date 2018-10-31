package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.dex.Register;

import java.util.List;

/**
 * Dex equivalent of jvm invoke dynamic instruction.
 *
 * A bootstrap method is initially used to resolves a CallSite.
 * This CallSite get called by this instruction.
 */
public class InvokeCustom extends Invoke {
    /**
     * An arbitrary string passed to the bootstrap method.
     */
    private String name;

    /**
     * Parameter and return type of the resolved CallSite.
     */
    private MethodDescriptor descriptor;

    /**
     * Additional arguments passed to the bootstrap method.
     */
    private List<BootstrapConstant> bootstrapArguments;

    /**
     * Boostrap method that gets invoked to resolve the CallSite.
     */
    private Handle bootstrapMethod;

    public InvokeCustom(List<Register> arguments, String name, MethodDescriptor descriptor, List<BootstrapConstant> bootstrapArguments, Handle bootstrapMethod) {
        super(arguments);
        this.name = name;
        this.descriptor = descriptor;
        this.bootstrapArguments = bootstrapArguments;
        this.bootstrapMethod = bootstrapMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public MethodDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public List<BootstrapConstant> getBootstrapArguments() {
        return bootstrapArguments;
    }

    public void setBootstrapArguments(List<BootstrapConstant> bootstrapArguments) {
        this.bootstrapArguments = bootstrapArguments;
    }

    public Handle getBootstrapMethod() {
        return bootstrapMethod;
    }

    public void setBootstrapMethod(Handle bootstrapMethod) {
        this.bootstrapMethod = bootstrapMethod;
    }

    @Override
    public List<Register> getRegisterReads() {
        return List.copyOf(getArguments());
    }
}
