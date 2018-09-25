package me.aki.tactical.core.handle;

import me.aki.tactical.core.MethodRef;

public class NewInstanceHandle extends AbstractMethodHandle implements BootstrapMethodHandle {
    public NewInstanceHandle(MethodRef methodRef) {
        super(methodRef);
    }
}
