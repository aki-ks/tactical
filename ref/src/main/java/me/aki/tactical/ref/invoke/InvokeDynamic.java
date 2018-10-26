package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.ref.Expression;

import java.lang.invoke.CallSite;
import java.util.List;
import java.util.Objects;

/**
 * Invoke a runtime resolved method.
 *
 * Once this invoke is executed the first time, the bootstrap method gets invoked.
 * The returned {@link CallSite} will from now on be invoked.
 */
public class InvokeDynamic extends AbstractInvoke {
    /**
     * An arbitrary name that is passed to the bootstrap method.
     */
    private String name;

    /**
     * Parameter and return type of the {@link CallSite} that will be invoked.
     */
    private MethodDescriptor descriptor;

    /**
     * The Bootstrap method invoked by the jvm to resolve the {@link CallSite}.
     */
    private Handle bootstrapMethod;

    /**
     * Additional arguments passed to the bootstrap method.
     */
    private List<BootstrapConstant> bootstrapArguments;

    public InvokeDynamic(String name, MethodDescriptor descriptor, Handle bootstrapMethod,
                         List<BootstrapConstant> bootstrapArguments, List<Expression> arguments) {
        super(arguments);
        this.name = name;
        this.descriptor = descriptor;
        this.bootstrapMethod = bootstrapMethod;
        this.bootstrapArguments = bootstrapArguments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MethodDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public Handle getBootstrapMethod() {
        return bootstrapMethod;
    }

    public void setBootstrapMethod(Handle bootstrapMethod) {
        this.bootstrapMethod = bootstrapMethod;
    }

    public List<BootstrapConstant> getBootstrapArguments() {
        return bootstrapArguments;
    }

    public void setBootstrapArguments(List<BootstrapConstant> bootstrapArguments) {
        this.bootstrapArguments = bootstrapArguments;
    }

    @Override
    public MethodDescriptor getMethodDescriptor() {
        return descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InvokeDynamic that = (InvokeDynamic) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(descriptor, that.descriptor) &&
                Objects.equals(bootstrapMethod, that.bootstrapMethod) &&
                Objects.equals(bootstrapArguments, that.bootstrapArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, descriptor, bootstrapMethod, bootstrapArguments);
    }
}
