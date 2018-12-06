package me.aki.tactical.conversion.asmutils;

import me.aki.tactical.conversion.asm2stack.ConversionContext;
import me.aki.tactical.stack.utils.AbstractStackInsnWriter;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.insn.Instruction;
import org.objectweb.asm.tree.LabelNode;

/**
 * Instruction visitor that collects all events represented as {@link Instruction}.
 */
public class AsmStackInsnWriter extends AbstractStackInsnWriter<LabelNode, Integer> {
    private final ConversionContext ctx;

    public AsmStackInsnWriter(ConversionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void registerTargetCell(LabelNode label, Cell<Instruction> cell) {
        ctx.registerForwardInsnCell(label, cell);
    }

    @Override
    public StackLocal convertLocal(Integer localIndex) {
        return ctx.getLocal(localIndex);
    }
}
