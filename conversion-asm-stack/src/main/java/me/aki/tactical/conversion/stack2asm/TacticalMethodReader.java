package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.core.Method;
import org.objectweb.asm.MethodVisitor;

public class TacticalMethodReader {
    private final Method method;

    public TacticalMethodReader(Method method) {
        this.method = method;
    }

    public void accept(MethodVisitor mv) {
        mv.visitEnd();
    }
}
