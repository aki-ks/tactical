package me.aki.tactical.stack.insn;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.constant.BootstrapConstant;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

/**
 * Invoke a method that is not statically known.
 *
 * The first time that the invokedynamic instruction is executed a bootstrap method is invoked.
 * This bootstrap method returns a {@link CallSite} that is cached by the JVM and
 * will be called on every execution of that invokedynamic instruction.
 *
 * At least three parameters of types {@link MethodHandles.Lookup}, {@link String} and
 * {@link MethodType} will be passed to bootstrap methods.
 * Some of these parameters might be caught in a varargs parameter.
 */
public class InvokeDynamicInsn implements BranchInsn {
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
     *
     * It is either a static method returning a {@link CallSite} or
     * a constructor of a class that extends {@link CallSite}.
     */
    private MethodRef bootstrapMethod;

    /**
     * Additional arguments passed to the bootstrap method.
     */
    private List<BootstrapConstant> bootstrapArguments;

    public InvokeDynamicInsn(String name, MethodDescriptor descriptor, MethodRef bootstrapMethod, List<BootstrapConstant> bootstrapArguments) {
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

    public MethodRef getBootstrapMethod() {
        return bootstrapMethod;
    }

    public void setBootstrapMethod(MethodRef bootstrapMethod) {
        this.bootstrapMethod = bootstrapMethod;
    }

    public List<BootstrapConstant> getBootstrapArguments() {
        return bootstrapArguments;
    }

    public void setBootstrapArguments(List<BootstrapConstant> bootstrapArguments) {
        this.bootstrapArguments = bootstrapArguments;
    }

    @Override
    public int getPushCount() {
        return descriptor.getReturnType().isPresent() ? 1 : 0;
    }

    @Override
    public int getPopCount() {
        return descriptor.getParameterTypes().size();
    }
}
