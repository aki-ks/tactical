package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.lang.invoke.CallSite;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Invoke a runtime resolved method.
 *
 * Once this invoke is executed the first time, the bootstrap method gets invoked.
 * The returned {@link CallSite} will from now on be invoked.
 */
public class InvokeDynamic implements Invoke {
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

    /**
     * Arguments passed to the invoked {@link CallSite}.
     */
    private List<Expression> arguments;

    public InvokeDynamic(String name, MethodDescriptor descriptor, Handle bootstrapMethod,
                         List<BootstrapConstant> bootstrapArguments, List<Expression> arguments) {
        this.name = name;
        this.descriptor = descriptor;
        this.bootstrapMethod = bootstrapMethod;
        this.bootstrapArguments = bootstrapArguments;
        this.arguments = arguments;
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

    public List<Expression> getArguments() {
        return arguments;
    }

    public void setArguments(List<Expression> arguments) {
        this.arguments = arguments;
    }

    public List<Cell<Expression>> getArgumentCells() {
        return IntStream.range(0, arguments.size())
                .mapToObj(index -> Cell.ofList(arguments, index))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public MethodDescriptor getMethodDescriptor() {
        return descriptor;
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return getArgumentCells();
    }
}
