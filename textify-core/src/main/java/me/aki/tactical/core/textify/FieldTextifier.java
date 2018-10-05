package me.aki.tactical.core.textify;

import me.aki.tactical.core.Field;

public class FieldTextifier implements Textifier<Field> {
    private static final FieldTextifier INSTANCE = new FieldTextifier();

    public static FieldTextifier getInstance() {
        return INSTANCE;
    }

    private FieldTextifier() {}

    @Override
    public void textify(Printer printer, Field field) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }
}
