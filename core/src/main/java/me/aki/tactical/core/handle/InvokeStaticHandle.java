package me.aki.tactical.core.handle;

import me.aki.tactical.core.MethodRef;

public class InvokeStaticHandle extends AbstractAmbiguousMethodHandle implements BootstrapMethodHandle {
    public InvokeStaticHandle(MethodRef methodRef, boolean isInterface) {
        super(methodRef, isInterface);
    }
}
