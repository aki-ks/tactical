package me.aki.tactical.stack;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.Type;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
                "isInterface=" + isInterface +
                '}';
    }
}
