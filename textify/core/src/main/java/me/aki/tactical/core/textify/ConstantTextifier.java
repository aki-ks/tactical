package me.aki.tactical.core.textify;

import me.aki.tactical.core.constant.*;

import java.util.List;

public class ConstantTextifier {
    public static final Textifier<NullConstant> NULL = (printer, constant) -> printer.addText("null");
    public static final Textifier<IntConstant> INT = (printer, constant) -> printer.addText(Integer.toString(constant.getValue()));
    public static final Textifier<LongConstant> LONG = (printer, constant) -> printer.addText(constant.getValue() + "l");
    public static final Textifier<FloatConstant> FLOAT = (printer, constant) -> printer.addText(constant.getValue() + "f");
    public static final Textifier<DoubleConstant> DOUBLE = (printer, constant) -> printer.addText(constant.getValue() + "d");
    public static final Textifier<StringConstant> STRING = (printer, constant) -> printer.addEscaped(constant.getValue(), '"');

    public static final Textifier<ClassConstant> CLASS = (printer, constant) -> {
        TypeTextifier.REF_TYPE.textify(printer, constant.getValue());
        printer.addText(".class");
    };

    public static final Textifier<MethodTypeConstant> METHOD_TYPE = (printer, constant) -> {
        printer.addText("method { ");
        MethodDescriptorTextifier.getInstance().textify(printer, constant.toDescriptor());
        printer.addText(" }");
    };

    public static final Textifier<HandleConstant> HANDLE = (printer, constant) -> {
        printer.addText("handle { ");
        HandleTextifier.getInstance().textify(printer, constant.getHandle());
        printer.addText(" }");
    };

    public static final Textifier<BootstrapConstant> BOOTSTRAP = (printer, constant) -> {
        if (constant instanceof IntConstant) {
            INT.textify(printer, (IntConstant) constant);
        } else if (constant instanceof LongConstant) {
            LONG.textify(printer, (LongConstant) constant);
        } else if (constant instanceof FloatConstant) {
            FLOAT.textify(printer, (FloatConstant) constant);
        } else if (constant instanceof DoubleConstant) {
            DOUBLE.textify(printer, (DoubleConstant) constant);
        } else if (constant instanceof StringConstant) {
            STRING.textify(printer, (StringConstant) constant);
        } else if (constant instanceof ClassConstant) {
            CLASS.textify(printer, (ClassConstant) constant);
        } else if (constant instanceof MethodTypeConstant) {
            METHOD_TYPE.textify(printer, (MethodTypeConstant) constant);
        } else if (constant instanceof HandleConstant) {
            HANDLE.textify(printer, (HandleConstant) constant);
        } else {
            throw new AssertionError();
        }
    };

    public static final Textifier<DynamicConstant> DYNAMIC = (printer, constant) -> {
        printer.addText("dynamic { name = ");
        printer.addEscaped(constant.getName(), '"');
        printer.addText(", type = ");
        TypeTextifier.getInstance().textify(printer, constant.getType());
        printer.addText(", bootstrap = ");
        HandleTextifier.getInstance().textify(printer, constant.getBootstrapMethod());

        List<BootstrapConstant> arguments = constant.getBootstrapArguments();
        if (!arguments.isEmpty()) {
            printer.addText(", arguments = [ ");
            TextUtil.joined(arguments,
                    argument -> BOOTSTRAP.textify(printer, argument),
                    () -> printer.addText(", "));
            printer.addText(" ]");
        }

        printer.addText(" }");
    };

    public static final Textifier<FieldConstant> FIELD = (printer, constant) -> {
        if (constant instanceof IntConstant) {
            INT.textify(printer, (IntConstant) constant);
        } else if (constant instanceof LongConstant) {
            LONG.textify(printer, (LongConstant) constant);
        } else if (constant instanceof FloatConstant) {
            FLOAT.textify(printer, (FloatConstant) constant);
        } else if (constant instanceof DoubleConstant) {
            DOUBLE.textify(printer, (DoubleConstant) constant);
        } else if (constant instanceof StringConstant) {
            STRING.textify(printer, (StringConstant) constant);
        } else {
            throw new AssertionError();
        }
    };

    public static final Textifier<PushableConstant> PUSHABLE = (printer, constant) -> {
        if (constant instanceof NullConstant) {
            NULL.textify(printer, (NullConstant) constant);
        } else if (constant instanceof PrimitiveValueConstant) {
            if (constant instanceof IntConstant) {
                INT.textify(printer, (IntConstant) constant);
            } else if (constant instanceof LongConstant) {
                LONG.textify(printer, (LongConstant) constant);
            } else if (constant instanceof FloatConstant) {
                FLOAT.textify(printer, (FloatConstant) constant);
            } else if (constant instanceof DoubleConstant) {
                DOUBLE.textify(printer, (DoubleConstant) constant);
            } else {
                throw new AssertionError();
            }
        } else if (constant instanceof StringConstant) {
            STRING.textify(printer, (StringConstant) constant);
        } else if (constant instanceof ClassConstant) {
            CLASS.textify(printer, (ClassConstant) constant);
        } else if (constant instanceof MethodTypeConstant) {
            METHOD_TYPE.textify(printer, (MethodTypeConstant) constant);
        } else if (constant instanceof HandleConstant) {
            HANDLE.textify(printer, (HandleConstant) constant);
        } else if (constant instanceof DynamicConstant) {
            DYNAMIC.textify(printer, (DynamicConstant) constant);
        } else {
            throw new AssertionError();
        }
    };

    public static final Textifier<DexConstant> DEX = (printer, constant) -> {
        if (constant instanceof NullConstant) {
            NULL.textify(printer, (NullConstant) constant);
        } else if (constant instanceof PrimitiveValueConstant) {
            if (constant instanceof IntConstant) {
                INT.textify(printer, (IntConstant) constant);
            } else if (constant instanceof LongConstant) {
                LONG.textify(printer, (LongConstant) constant);
            } else if (constant instanceof FloatConstant) {
                FLOAT.textify(printer, (FloatConstant) constant);
            } else if (constant instanceof DoubleConstant) {
                DOUBLE.textify(printer, (DoubleConstant) constant);
            } else {
                throw new AssertionError();
            }
        } else if (constant instanceof StringConstant) {
            STRING.textify(printer, (StringConstant) constant);
        } else if (constant instanceof ClassConstant) {
            CLASS.textify(printer, (ClassConstant) constant);
        } else if (constant instanceof MethodTypeConstant) {
            METHOD_TYPE.textify(printer, (MethodTypeConstant) constant);
        } else if (constant instanceof HandleConstant) {
            HANDLE.textify(printer, (HandleConstant) constant);
        } else {
            throw new AssertionError();
        }
    };

}
