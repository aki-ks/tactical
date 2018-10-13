package me.aki.tactical.dex.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.dex.Register;

import java.lang.invoke.MethodHandle;
import java.util.List;

/**
 * Invoke a signature polymorphic method.
 *
 * These method must be declared with one varargs <tt>Object...</tt> parameter.
 * The parameter types in the {@link MethodRef} are the onces that are passed to the method.
 *
 * This method is used to invoke {@link MethodHandle#invoke(Object...)} and
 * {@link MethodHandle#invokeExact(Object...)}.
 */
public class InvokePolymorphic extends InstanceInvoke {
    public InvokePolymorphic(MethodRef method, Register instance, List<Register> arguments) {
        super(method, instance, arguments);
    }
}
