package me.aki.tactical.dex.textifier;

import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.dex.insn.Instruction;

public class InstructionTextifier implements CtxTextifier<Instruction> {
    private static final InstructionTextifier INSTANCE = new InstructionTextifier();

    public static InstructionTextifier getInstance() {
        return INSTANCE;
    }

    @Override
    public void textify(Printer printer, TextifyCtx ctx, Instruction value) {

    }
}
