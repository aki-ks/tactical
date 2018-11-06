package me.aki.tactical.ref.textifier;

import me.aki.tactical.core.textify.Printer;

public interface CtxTextifier<T> {
    public void textify(Printer printer, TextifyCtx ctx, T value);
}
