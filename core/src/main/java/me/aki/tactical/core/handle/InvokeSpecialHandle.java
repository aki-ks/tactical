package me.aki.tactical.core.handle;

import me.aki.tactical.core.MethodRef;

public class InvokeSpecialHandle extends AbstractAmbiguousMethodHandle {
    public InvokeSpecialHandle(MethodRef methodRef, boolean isInterface) {
        super(methodRef, isInterface);
    }
}
