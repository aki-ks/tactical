package me.aki.tactical.core.constant;

import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.type.Type;

import java.lang.invoke.CallSite;
import java.util.List;
import java.util.Objects;

/**
 * A constant that will be dynamically computed at runtime.
 *
 * Once the constant gets pushed the first time, the bootstrap method will be called.
 * It returns a static {@link CallSite} that will be used to get the constant.
 */
public class DynamicConstant implements PushableConstant, Constant {
    /**
     * An arbitrary name passed to the bootstrap method.
     */
    private final String name;

    /**
     * The type of the produced constant.
     */
    private final Type type;

    /**
     * The bootstrap method invoked by the jvm to get a call site that produces the actual constant.
     */
    private final Handle bootstrapMethod;

    /**
     * Additional arguments passed to the bootstrap method.
     */
    private final List<BootstrapConstant> bootstrapArguments;

    public DynamicConstant(String name, Type type, Handle bootstrapMethod, List<BootstrapConstant> bootstrapArguments) {
        this.name = name;
        this.type = type;
        this.bootstrapMethod = bootstrapMethod;
        this.bootstrapArguments = List.copyOf(bootstrapArguments);
    }

    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    public Handle getBootstrapMethod() {
        return bootstrapMethod;
    }

    public List<BootstrapConstant> getBootstrapArguments() {
        return bootstrapArguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicConstant that = (DynamicConstant) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(bootstrapMethod, that.bootstrapMethod) &&
                Objects.equals(bootstrapArguments, that.bootstrapArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, bootstrapMethod, bootstrapArguments);
    }

    @Override
    public String toString() {
        return DynamicConstant.class.getSimpleName() + '{' +
                "name='" + name + '\'' +
                ", type=" + type +
                ", bootstrapMethod=" + bootstrapMethod +
                ", bootstrapArguments=" + bootstrapArguments +
                '}';
    }
}
