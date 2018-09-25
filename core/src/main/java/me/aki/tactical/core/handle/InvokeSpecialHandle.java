package me.aki.tactical.core.handle;

import me.aki.tactical.core.InvokableMethodRef;
import me.aki.tactical.core.MethodRef;

public class InvokeSpecialHandle extends AbstractAmbiguousMethodHandle {
    public InvokeSpecialHandle(MethodRef methodRef, boolean isInterface) {
        super(new InvokableMethodRef(methodRef.getOwner(), methodRef.getName(), methodRef.getArguments(), methodRef.getReturnType(), isInterface));
    }

    public InvokeSpecialHandle(InvokableMethodRef methodRef) {
        super(methodRef);
    }
}
