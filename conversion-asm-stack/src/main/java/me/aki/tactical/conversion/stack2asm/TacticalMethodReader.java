package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stackasm.AccessConverter;
import me.aki.tactical.core.Method;
import org.objectweb.asm.MethodVisitor;

public class TacticalMethodReader {
    private final Method method;

    public TacticalMethodReader(Method method) {
        this.method = method;
    }

    public void accept(MethodVisitor mv) {
        visitParameters(mv);
        mv.visitEnd();
    }

    private void visitParameters(MethodVisitor mv) {
        for (Method.Parameter parameter : method.getParameterInfo()) {
            String name = parameter.getName().orElse(null);
            int access = AccessConverter.parameter.toBitMap(parameter.getFlags());

            mv.visitParameter(name, access);
        }
    }
}
