package me.aki.tactical.ref.textifier;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.textify.ConstantTextifier;
import me.aki.tactical.core.textify.HandleTextifier;
import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.TextUtil;
import me.aki.tactical.core.textify.TypeTextifier;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.invoke.AbstractConcreteInvoke;
import me.aki.tactical.ref.invoke.AbstractInstanceInvoke;
import me.aki.tactical.ref.invoke.AbstractInvoke;
import me.aki.tactical.ref.invoke.InvokeDynamic;
import me.aki.tactical.ref.invoke.InvokeInterface;
import me.aki.tactical.ref.invoke.InvokeSpecial;
import me.aki.tactical.ref.invoke.InvokeStatic;
import me.aki.tactical.ref.invoke.InvokeVirtual;

import java.util.Iterator;
import java.util.List;

public class InvokeTextifier implements CtxTextifier<AbstractInvoke> {
    private static final InvokeTextifier INSTANCE = new InvokeTextifier();

    public static InvokeTextifier getInstance() {
        return INSTANCE;
    }

    public static final CtxTextifier<InvokeDynamic> DYNAMIC = (printer, ctx, invoke) -> {
        printer.addText("invoke dynamic { name = ");
        printer.addEscaped(invoke.getName(), '"');

        printer.addText(", type = ");
        textifyDescriptorWithArguments(printer, ctx, invoke.getDescriptor(), invoke.getArguments());

        printer.addText(", bootstrap = ");
        HandleTextifier.getInstance().textify(printer, invoke.getBootstrapMethod());

        printer.addText(", arguments = ");
        List<BootstrapConstant> bootstrapArguments = invoke.getBootstrapArguments();
        if (bootstrapArguments.isEmpty()) {
            printer.addText("[]");
        } else {
            printer.addText("[ ");
            TextUtil.joined(bootstrapArguments,
                    constant -> ConstantTextifier.BOOTSTRAP.textify(printer, constant),
                    () -> printer.addText(", "));
            printer.addText(" ]");
        }

        printer.addText(" }");
    };

    private static void textifyDescriptorWithArguments(Printer printer, TextifyCtx ctx, MethodDescriptor descriptor, List<Expression> parameters) {
        TextUtil.biJoined(descriptor.getParameterTypes(), parameters,
                (parameterType, parameter) -> {
                    ExpressionTextifier.getInstance().textify(printer, ctx, parameter);
                    printer.addText(" : ");
                    TypeTextifier.getInstance().textify(printer, parameterType);
                },
                () -> printer.addText(", "));

        printer.addText(" : ");

        descriptor.getReturnType().ifPresentOrElse(
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText("void"));
    }

    public static final CtxTextifier<AbstractConcreteInvoke> CONCRETE = (printer, ctx, invoke) -> {
        MethodRef method = invoke.getMethod();

        printer.addPath(method.getOwner());
        printer.addText(".<");

        String keyword;
        if (invoke instanceof InvokeInterface) {
            keyword = "interface ";
        } else if (invoke instanceof InvokeSpecial) {
            boolean isInterface = ((InvokeSpecial) invoke).isInterface();
            keyword = "special " + (isInterface ? "interface " : "");
        } else if (invoke instanceof InvokeStatic) {
            boolean isInterface = ((InvokeStatic) invoke).isInterface();
            keyword = "static " + (isInterface ? "interface " : "");
        } else if (invoke instanceof InvokeVirtual) {
            keyword = "virtual ";
        } else {
            throw new AssertionError();
        }
        printer.addText(keyword);

        if (invoke instanceof AbstractInstanceInvoke) {
            Expression instance = ((AbstractInstanceInvoke) invoke).getInstance();
            ExpressionTextifier.getInstance().textify(printer, ctx, instance);
        }
        printer.addText(">.");

        printer.addLiteral(method.getName());
        printer.addText("(");

        TextUtil.biJoined(invoke.getArguments(), method.getArguments(),
                (argument, argumentType) -> {
                    ExpressionTextifier.getInstance().textify(printer, ctx, argument);
                    printer.addText(" : ");
                    TypeTextifier.getInstance().textify(printer, argumentType);
                },
                () -> printer.addText(", "));

        printer.addText(") : ");
        method.getReturnType().ifPresentOrElse(
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText("void"));
    };

    @Override
    public void textify(Printer printer, TextifyCtx ctx, AbstractInvoke invoke) {
        if (invoke instanceof AbstractConcreteInvoke) {
            CONCRETE.textify(printer, ctx, (AbstractConcreteInvoke) invoke);
        } else if (invoke instanceof InvokeDynamic) {
            DYNAMIC.textify(printer, ctx, (InvokeDynamic) invoke);
        } else {
            throw new AssertionError();
        }
    }
}
