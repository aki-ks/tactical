package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.dex.Register;

import java.util.List;

/**
 * Invoke a method on an instance of an interface
 */
public class InvokeInterface extends InstanceInvoke {
    public InvokeInterface(MethodRef method, Register instance, List<Register> arguments) {
        super(method, instance, arguments);
    }
}
