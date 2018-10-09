package me.aki.tactical.core.textify;

import me.aki.tactical.core.MethodDescriptor;

public class MethodDescriptorTextifier implements Textifier<MethodDescriptor> {
    private static final MethodDescriptorTextifier INSTANCE = new MethodDescriptorTextifier();

    public static MethodDescriptorTextifier getInstance() {
        return INSTANCE;
    }

    private MethodDescriptorTextifier() {}

    @Override
    public void textify(Printer printer, MethodDescriptor descriptor) {
        printer.addText("(");

        TextUtil.joined(descriptor.getParameterTypes(),
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText(", "));

        printer.addText(") ");

        descriptor.getReturnType().ifPresentOrElse(
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText("void"));
    }
}
