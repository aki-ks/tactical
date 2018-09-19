package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.Method;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.TryCatchBlock;
import me.aki.tactical.stack.insn.Instruction;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BodyConverter {
    private final Method method;
    private final StackBody body;
    private final MethodNode mn;

    private final ConversionContext ctx = new ConversionContext();

    private final Map<AbstractInsnNode, Instruction> convertedInsns = new HashMap<>();

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

        //TODO Do conversion

        updateInsnCells();
    }

    private void updateInsnCells() {
        ctx.getLabelCells().forEach((label, insnCells) -> {
            convertedInsns.get(label);

        });
    }
}
