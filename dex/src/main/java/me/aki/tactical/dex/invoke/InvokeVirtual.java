package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.dex.Register;

import java.util.List;

/**
 * Invoke a non-private, non-static, non-final method on a instance of a class.
 */
public class InvokeVirtual extends InstanceInvoke {
    public InvokeVirtual(MethodRef method, Register instance, List<Register> arguments) {
        super(method, instance, arguments);
    }
}
