package me.aki.tactical.core.textify;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.handle.AbstractAmbiguousMethodHandle;
import me.aki.tactical.core.handle.FieldHandle;
import me.aki.tactical.core.handle.GetFieldHandle;
import me.aki.tactical.core.handle.GetStaticHandle;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.handle.InvokeInterfaceHandle;
import me.aki.tactical.core.handle.InvokeSpecialHandle;
import me.aki.tactical.core.handle.InvokeStaticHandle;
import me.aki.tactical.core.handle.InvokeVirtualHandle;
import me.aki.tactical.core.handle.MethodHandle;
import me.aki.tactical.core.handle.NewInstanceHandle;
import me.aki.tactical.core.handle.SetStaticHandle;

public class HandleTextifier implements Textifier<Handle> {
    private static HandleTextifier INSTANCE = new HandleTextifier();

    public static HandleTextifier getInstance() {
        return INSTANCE;
    }

    private HandleTextifier() {}

    public static final Textifier<FieldHandle> FIELD_HANDLE = (printer, handle) -> {
        boolean isGet = handle instanceof GetFieldHandle || handle instanceof GetStaticHandle;
        boolean isStatic = handle instanceof GetStaticHandle || handle instanceof SetStaticHandle;

        printer.addText(isGet ? "get " : "set ");
        if (isStatic) {
            printer.addText("static ");
        }

        FieldRef field = handle.getFieldRef();
        printer.addPath(field.getOwner());
        printer.addText(".");
        printer.addLiteral(field.getName());
        printer.addText(" : ");
        TypeTextifier.getInstance().textify(printer, field.getType());
    };

    public static final Textifier<MethodHandle> METHOD_HANDLE = (printer, handle) -> {
        printer.addText("invoke ");
        printer.addText(
                handle instanceof InvokeInterfaceHandle ? "interface " :
                handle instanceof InvokeSpecialHandle ? "special " :
                handle instanceof InvokeStaticHandle ? "static " :
                handle instanceof InvokeVirtualHandle ? "virtual " :
                handle instanceof NewInstanceHandle ? "new " :
                TextUtil.assertionError()
        );

        if (handle instanceof AbstractAmbiguousMethodHandle &&
                ((AbstractAmbiguousMethodHandle) handle).isInterface()) {
            printer.addText("interface ");
        }

        MethodRef method = handle.getMethodRef();
        printer.addPath(method.getOwner());
        printer.addText(".");
        printer.addLiteral(method.getName());
        printer.addText("(");
        TextUtil.joined(method.getArguments(),
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText(", "));
        printer.addText(") : ");
        method.getReturnType().ifPresentOrElse(
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText("void"));
    };

    @Override
    public void textify(Printer printer, Handle handle) {
        if (handle instanceof FieldHandle) {
            FIELD_HANDLE.textify(printer, (FieldHandle) handle);
        } else if (handle instanceof MethodHandle) {
            METHOD_HANDLE.textify(printer, (MethodHandle) handle);
        } else {
            throw new AssertionError();
        }
    }
}
