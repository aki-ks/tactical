package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.dex.Register;

import java.util.List;

/**
 * Invoke a non-static non-overridable method.
 */
public class InvokeDirect extends InstanceInvoke {
    public InvokeDirect(MethodRef method, Register instance, List<Register> arguments) {
        super(method, instance, arguments);
    }
}
