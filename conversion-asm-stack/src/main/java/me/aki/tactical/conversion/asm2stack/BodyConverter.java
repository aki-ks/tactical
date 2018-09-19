package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.Method;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.StackBody;
import org.objectweb.asm.tree.MethodNode;

import java.util.Optional;

public class BodyConverter {
    private final Method method;
    private final StackBody body;
    private final MethodNode mn;

    private final ConversionContext ctx = new ConversionContext();

    public BodyConverter(Method method, StackBody body, MethodNode mn) {
        this.method = method;
        this.body = body;
        this.mn = mn;
    }

    private void initLocals() {
        for (int i = 0; i < mn.maxLocals; i++) {
            ctx.getLocals().add(new Local());
        }

        int localIndex = 0;
        if (this.method.getFlag(Method.Flag.STATIC)) {
            this.body.setThisLocal(Optional.of(ctx.getLocal(localIndex++)));
        }

        for (Type paramType : this.method.getParameterTypes()) {
            this.body.getParameterLocals().add(ctx.getLocal(localIndex++));
        }
    }

    public void convert() {
        initLocals();
    }
}
