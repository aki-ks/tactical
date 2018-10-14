package me.aki.tactical.stack.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.handle.Handle;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;

/**
 * Invoke a dynamically computed method.
 *
 * The first time that the invokedynamic instruction is executed a bootstrap method is invoked.
 * This bootstrap method returns a {@link CallSite} that is cached by the JVM and
 * will be called on every execution of that invokedynamic instruction.
 *
 * At least three parameters of types {@link MethodHandles.Lookup}, {@link String} and
 * {@link MethodType} will be passed to bootstrap methods.
 * Some of these parameters might be caught in a varargs parameter.
 */
public class DynamicInvoke implements Invoke {
    /**
     * The {@link String} parameter that is passed to the bootstrap method.
     *
     * It is unrelated to name of the target behind the resolved {@link CallSite}.
     */
    private String name;

    /**
     * The {@link MethodType} parameter that is passed to the bootstrap method.
     *
     * This signature is semantically equivalent to the resolved {@link CallSite}.
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

    public DynamicInvoke(String name, MethodDescriptor descriptor, Handle bootstrapMethod, List<BootstrapConstant> bootstrapArguments) {
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

    @Override
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicInvoke that = (DynamicInvoke) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(descriptor, that.descriptor) &&
                Objects.equals(bootstrapMethod, that.bootstrapMethod) &&
                Objects.equals(bootstrapArguments, that.bootstrapArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, descriptor, bootstrapMethod, bootstrapArguments);
    }
}
