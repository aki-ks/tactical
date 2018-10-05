package me.aki.tactical.core.textify;

import me.aki.tactical.core.Method;

public class MethodTextifier implements Textifier<Method> {
    private static final MethodTextifier INSTANCE = new MethodTextifier();

    public static MethodTextifier getInstance() {
        return INSTANCE;
    }

    private MethodTextifier() {}

    @Override
    public void textify(Printer printer, Method method) {
        throw new RuntimeException("Not yet implemented");
    }
}
