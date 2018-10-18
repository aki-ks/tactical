package me.aki.tactical.ref.textifier;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.textify.ConstantTextifier;
import me.aki.tactical.core.textify.HandleTextifier;
import me.aki.tactical.core.textify.MethodDescriptorTextifier;
import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.TextUtil;
import me.aki.tactical.core.textify.TypeTextifier;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.invoke.AbstractConcreteInvoke;
import me.aki.tactical.ref.invoke.AbstractInstanceInvoke;
import me.aki.tactical.ref.invoke.AbstractInvoke;
import me.aki.tactical.ref.invoke.AmbigiousInvoke;
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
        if (descriptor.getParameterTypes().size() != parameters.size()) {
            throw new IllegalArgumentException("");
        }

        Iterator<Type> paramTypeIter = descriptor.getParameterTypes().iterator();
        Iterator<Expression> paramIter = parameters.iterator();

        while (paramIter.hasNext()) {
            ExpressionTextifier.getInstance().textify(printer, ctx, paramIter.next());
            printer.addText(" : ");
            TypeTextifier.getInstance().textify(printer, paramTypeIter.next());

            if(paramIter.hasNext()) {
                printer.addText(", ");
            }
        }

        printer.addText(" : ");
        descriptor.getReturnType().ifPresentOrElse(
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText("void"));
    }

    public static final CtxTextifier<AbstractConcreteInvoke> CONCRETE = (printer, ctx, invoke) -> {
        boolean isInterface = invoke instanceof AmbigiousInvoke && ((AmbigiousInvoke) invoke).isInterface();
        boolean isInstanceInvoke = invoke instanceof AbstractInstanceInvoke;
        MethodRef method = invoke.getMethod();

        String keyword;
        if (invoke instanceof InvokeInterface) {
            keyword = "invoke interface ";
        } else if (invoke instanceof InvokeSpecial) {
            keyword = "invoke special ";
        } else if (invoke instanceof InvokeStatic) {
            keyword = "invoke static ";
        } else if (invoke instanceof InvokeVirtual) {
            keyword = "inovke virtual ";
        } else {
            throw new AssertionError();
        }
        printer.addText(keyword);

        printer.addPath(method.getOwner());
        printer.addText(".");

        if (isInterface || isInstanceInvoke) {
            printer.addText("<");

            if (isInterface) {
                printer.addText("interface");
            }

            if (isInterface && isInstanceInvoke) {
                printer.addText(" ");
            }

            if (isInstanceInvoke) {
                ExpressionTextifier.getInstance().textify(printer, ctx, ((AbstractInstanceInvoke) invoke).getInstance());
            }

            printer.addText(">.");
        }

        printer.addLiteral(method.getName());
        printer.addText("(");

        textifyArguments(printer, ctx, method.getArguments(), invoke.getArguments());

        printer.addText(") : ");
        method.getReturnType().ifPresentOrElse(
                type -> TypeTextifier.getInstance().textify(printer, type),
                () -> printer.addText("void"));
    };

    private static void textifyArguments(Printer printer, TextifyCtx ctx, List<Type> argumentTypes, List<Expression> arguments) {
        if (argumentTypes.size() != arguments.size()) {
            throw new IllegalArgumentException("Argument type count does not match argument expressions count");
        }

        Iterator<Type> argTypeIter = argumentTypes.iterator();
        Iterator<Expression> argIter = arguments.iterator();

        while (argIter.hasNext()) {
            ExpressionTextifier.getInstance().textify(printer, ctx, argIter.next());
            printer.addText(" : ");
            TypeTextifier.getInstance().textify(printer, argTypeIter.next());

            if (argIter.hasNext()) {
                printer.addText(", ");
            }
        }
    }

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
