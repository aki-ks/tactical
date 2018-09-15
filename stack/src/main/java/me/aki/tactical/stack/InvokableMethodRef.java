package me.aki.tactical.stack;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.Type;

import java.util.List;
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
}
