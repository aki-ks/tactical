package me.aki.tactical.conversion.ref2stack;

import me.aki.tactical.conversion.stackasm.AbstractStackInsnWriter;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.stack.insn.Instruction;

/**
 * Instruction visitor that collects all events represented as {@link Instruction} and
 * registers instruction references to a {@link ConversionContext}.
 */
public class StackInsnWriter extends AbstractStackInsnWriter<Statement> {
    private final ConversionContext ctx;

    public StackInsnWriter(ConversionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void registerTargetCell(Statement target, Cell<Instruction> cell) {
        this.ctx.registerInstructionReference(target, cell);
    }
}
