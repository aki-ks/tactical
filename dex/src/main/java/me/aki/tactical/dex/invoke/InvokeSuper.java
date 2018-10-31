package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.dex.Register;

import java.util.List;

/**
 * Invoke a method within the super class.
 */
public class InvokeSuper extends InstanceInvoke {
    public InvokeSuper(MethodRef method, Register instance, List<Register> arguments) {
        super(method, instance, arguments);
    }
}
