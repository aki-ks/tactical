package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.ref.Referencing;

public interface Invoke extends Referencing {
    /**
     * Get the argument and return type of the invoked method.
     *
     * @return descriptor of the invoked method
     */
    MethodDescriptor getMethodDescriptor();
}
