package me.aki.tactical.core.handle;

import me.aki.tactical.core.InvokableMethodRef;
import me.aki.tactical.core.MethodRef;

public class InvokeStaticHandle extends AbstractAmbiguousMethodHandle implements BootstrapMethodHandle {
    public InvokeStaticHandle(MethodRef methodRef, boolean isInterface) {
        super(new InvokableMethodRef(methodRef.getOwner(), methodRef.getName(), methodRef.getArguments(), methodRef.getReturnType(), isInterface));
    }

    public InvokeStaticHandle(InvokableMethodRef methodRef) {
        super(methodRef);
    }
}
