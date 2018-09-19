package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.StackBody;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BodyConverter {
    private final Method method;
    private final StackBody body;
    private final MethodNode mn;

    private List<Local> locals;

    public BodyConverter(Method method, StackBody body, MethodNode mn) {
        this.method = method;
        this.body = body;
        this.mn = mn;
    }

    private void initLocals() {
        this.locals = new ArrayList<>();
        for (int i = 0; i < mn.maxLocals; i++) {
            this.locals.add(new Local());
        }

        int localIndex = 0;
        if (this.method.getFlag(Method.Flag.STATIC)) {
            this.body.setThisLocal(Optional.of(getLocal(localIndex++)));
        }

        for (Type paramType : this.method.getParameterTypes()) {
            this.body.getParameterLocals().add(getLocal(localIndex++));
        }
    }

    private Local getLocal(int index) {
        return this.locals.get(index);
    }

    public void convert() {
        initLocals();

        convertInsns();
    }
}
