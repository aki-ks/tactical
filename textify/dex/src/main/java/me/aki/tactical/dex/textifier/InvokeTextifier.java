package me.aki.tactical.dex.textifier;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.textify.*;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.dex.Register;
import me.aki.tactical.dex.invoke.*;

import java.util.List;
import java.util.Optional;

public class InvokeTextifier implements CtxTextifier<Invoke> {

    private static final InvokeTextifier INSTANCE = new InvokeTextifier();

    public static CtxTextifier<Invoke> getInstance() {
        return INSTANCE;
    }

    private static final CtxTextifier<ConcreteInvoke> CONCRETE = (printer, ctx, invoke) -> {
        MethodRef method = invoke.getMethod();
        String invokeType = invoke instanceof InvokeDirect ? "direct" :
                invoke instanceof InvokeInterface ? "interface" :
                invoke instanceof InvokeStatic ? "static" :
                invoke instanceof InvokeSuper ? "super" :
                invoke instanceof InvokeVirtual ? "virtual" :
                invoke instanceof InvokePolymorphic ? "polymorphic" :
                TextUtil.assertionError();

        printer.addText("invoke " + invokeType + " ");

        printer.addPath(method.getOwner());
        printer.addText(".");
        if (invoke instanceof InstanceInvoke) {
            Register instance = ((InstanceInvoke) invoke).getInstance();
            printer.addText("<");
            printer.addLiteral(ctx.getRegisterName(instance));
            printer.addText(">.");
        }
        printer.addLiteral(method.getName());

        printArguments(printer, ctx, invoke.getArguments(), method.getArguments(), method.getReturnType());

        printer.addText(";");
    };

    private static final CtxTextifier<InvokeCustom> CUSTOM = new CtxTextifier<>() {
        @Override
        public void textify(Printer printer, TextifyCtx ctx, InvokeCustom invoke) {
            printer.addText("invoke custom { name = ");
            printer.addEscaped(invoke.getName(), '"');
            printer.addText(", arguments = ");
            printBootstrapArguments(printer, ctx, invoke.getBootstrapArguments());
            printer.addText(", handle = ");
            HandleTextifier.getInstance().textify(printer, invoke.getBootstrapMethod());
            printer.addText(" } ");

            MethodDescriptor descriptor = invoke.getDescriptor();
            printArguments(printer, ctx, invoke.getArguments(), descriptor.getParameterTypes(), descriptor.getReturnType());

            printer.addText(";");
        }

        private void printBootstrapArguments(Printer printer, TextifyCtx ctx, List<BootstrapConstant> bootstrapArguments) {
            if (bootstrapArguments.isEmpty()) {
                printer.addText("[]");
            } else {
                printer.addText("[ ");
                TextUtil.joined(bootstrapArguments,
                        constant -> ConstantTextifier.BOOTSTRAP.textify(printer, constant),
                        () -> printer.addText(", ")
                );
                printer.addText(" ]");
            }
        }
    };

    @Override
    public void textify(Printer printer, TextifyCtx ctx, Invoke invoke) {
        if (invoke instanceof InvokeCustom) {
            CUSTOM.textify(printer, ctx, (InvokeCustom) invoke);
        } else if (invoke instanceof ConcreteInvoke) {
            CONCRETE.textify(printer, ctx, (ConcreteInvoke) invoke);
        } else {
            TextUtil.assertionError();
        }
    }

    private static void printArguments(Printer printer, TextifyCtx ctx, List<Register> argumentsRegisters, List<Type> argumentTypes, Optional<Type> returnTypeOpt) {
        printer.addText("(");
        TextUtil.biJoined(argumentsRegisters, argumentTypes,
                (arg, argType) -> {
                    printer.addLiteral(ctx.getRegisterName(arg));
                    printer.addText(" : ");
                    TypeTextifier.getInstance().textify(printer, argType);
                },
                () ->  printer.addText(", ")
        );
        printer.addText(")");

        returnTypeOpt.ifPresent(returnType -> {
            printer.addText(" : ");
            TypeTextifier.getInstance().textify(printer, returnType);
        });
    }
}
