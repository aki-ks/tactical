package me.aki.tactical.core;

import me.aki.tactical.core.type.Type;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link MethodRef} that also stores whether the method is declared within an interface.
 */
public class InvokableMethodRef extends MethodRef {
    /**
     * Is this Method declared within an interface.
     *
     * This flag must also be set for static methods in interfaces.
     */
    private boolean isInterface;

    public InvokableMethodRef(Path owner, String name, List<Type> arguments, Optional<Type> returnType, boolean isInterface) {
        super(owner, name, arguments, returnType);
        this.isInterface = isInterface;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InvokableMethodRef that = (InvokableMethodRef) o;
        return isInterface == that.isInterface;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isInterface);
    }

    @Override
    public String toString() {
        return InvokableMethodRef.class.getSimpleName() + '{' +
                "owner=" + getOwner() +
                ", name='" + getName() + '\'' +
                ", arguments=" + getArguments() +
                ", returnType=" + getReturnType() +
                ", isInterface=" + isInterface +
                '}';
    }
}
