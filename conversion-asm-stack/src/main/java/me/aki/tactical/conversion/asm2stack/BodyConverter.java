package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.conversion.asm2stack.visitor.AsmInsnReader;
import me.aki.tactical.conversion.asm2stack.visitor.InsnWriter;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.StackBody;
import me.aki.tactical.stack.insn.Instruction;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BodyConverter {
    private final Classfile classfile;
    private final Method method;
    private final StackBody body;
    private final MethodNode mn;

    private final ConversionContext ctx = new ConversionContext();

    private final Map<AbstractInsnNode, List<Instruction>> convertedInsns = new HashMap<>();

    public BodyConverter(Classfile classfile, Method method, StackBody body, MethodNode mn) {
        this.classfile = classfile;
        this.method = method;
        this.body = body;
        this.mn = mn;
    }

    private void initLocals() {
        for (int i = 0; i < mn.maxLocals; i++) {
            ctx.getLocals().add(new Local());
        }

        int localIndex = 0;
        if (!this.method.getFlag(Method.Flag.STATIC)) {
            this.body.setThisLocal(Optional.of(ctx.getLocal(localIndex++)));
        }

        for (Type paramType : this.method.getParameterTypes()) {
            this.body.getParameterLocals().add(ctx.getLocal(localIndex++));
        }
    }

    public void convert() {
        initLocals();

        convertInsns();

        updateInsnCells();
    }

    private void convertInsns() {
        InsnWriter iw = new InsnWriter(ctx);
        String owner = classfile.getName().join('/');
        AsmInsnReader reader = new AsmInsnReader(iw, ctx, owner, mn);

        mn.instructions.iterator().forEachRemaining(insn -> {
            reader.accept(insn);

            List<Instruction> convertedInsns = iw.getInstructions();
            if (convertedInsns.isEmpty()) {
                // The converted instruction was dead code
            } else {
                this.convertedInsns.put(insn, new ArrayList<>(convertedInsns));
                this.body.getInstructions().addAll(convertedInsns);
                convertedInsns.clear();
            }
        });
    }

    private void updateInsnCells() {
        ctx.getLabelCells().forEach((label, insnCells) -> {
            List<Instruction> insns;

            AbstractInsnNode node = label;
            while (true) {
                insns = convertedInsns.get(node);
                if (insns != null && !insns.isEmpty()) {
                    break;
                }

                if ((node = node.getNext()) == null) {
                    throw new RuntimeException("Label could not be resolved");
                }
            }

            for (Cell<Instruction> cell : insnCells) {
                cell.set(insns.get(0));
            }
        });
    }
}
