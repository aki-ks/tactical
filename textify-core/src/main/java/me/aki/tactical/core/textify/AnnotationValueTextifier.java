package me.aki.tactical.core.textify;

import me.aki.tactical.core.annotation.AnnotationAnnotationValue;
import me.aki.tactical.core.annotation.AnnotationValue;
import me.aki.tactical.core.annotation.ArrayAnnotationValue;
import me.aki.tactical.core.annotation.BooleanAnnotationValue;
import me.aki.tactical.core.annotation.ByteAnnotationValue;
import me.aki.tactical.core.annotation.CharAnnotationValue;
import me.aki.tactical.core.annotation.ClassAnnotationValue;
import me.aki.tactical.core.annotation.DoubleAnnotationValue;
import me.aki.tactical.core.annotation.EnumAnnotationValue;
import me.aki.tactical.core.annotation.FloatAnnotationValue;
import me.aki.tactical.core.annotation.IntAnnotationValue;
import me.aki.tactical.core.annotation.LongAnnotationValue;
import me.aki.tactical.core.annotation.PrimitiveAnnotationValue;
import me.aki.tactical.core.annotation.ShortAnnotationValue;
import me.aki.tactical.core.annotation.StringAnnotationValue;

import java.util.List;

public class AnnotationValueTextifier implements Textifier<AnnotationValue> {
    private final static AnnotationValueTextifier INSTANCE = new AnnotationValueTextifier();
    public static AnnotationValueTextifier getInstance() {
        return INSTANCE;
    }

    public final static Textifier<BooleanAnnotationValue> BOOLEAN = (printer, value) -> printer.addText(value.getValue() ? "true" :  "false");
    public final static Textifier<ByteAnnotationValue> BYTE = (printer, value) -> printer.addText(value.getValue() + "b");
    public final static Textifier<ShortAnnotationValue> SHORT = (printer, value) -> printer.addText(value.getValue() + "s");
    public final static Textifier<IntAnnotationValue> INT = (printer, value) -> printer.addText(Integer.toString(value.getValue()));
    public final static Textifier<LongAnnotationValue> LONG = (printer, value) -> printer.addText(value.getValue() + "l");
    public final static Textifier<FloatAnnotationValue> FLOAT = (printer, value) -> printer.addText(value.getValue() + "f");
    public final static Textifier<DoubleAnnotationValue> DOUBLE = (printer, value) -> printer.addText(value.getValue() + "d");
    public final static Textifier<CharAnnotationValue> CHAR = (printer, value) ->
            printer.addEscaped(Character.toString(value.getValue()), '\'');

    public final static Textifier<PrimitiveAnnotationValue> PRIMITIVE = (printer, value) -> {
        if (value instanceof BooleanAnnotationValue) {
            BOOLEAN.textify(printer, (BooleanAnnotationValue) value);
        } else if (value instanceof ByteAnnotationValue) {
            BYTE.textify(printer, (ByteAnnotationValue) value);
        } else if (value instanceof ShortAnnotationValue) {
            SHORT.textify(printer, (ShortAnnotationValue) value);
        } else if (value instanceof CharAnnotationValue) {
            CHAR.textify(printer, (CharAnnotationValue) value);
        } else if (value instanceof IntAnnotationValue) {
            INT.textify(printer, (IntAnnotationValue) value);
        } else if (value instanceof LongAnnotationValue) {
            LONG.textify(printer, (LongAnnotationValue) value);
        } else if (value instanceof FloatAnnotationValue) {
            FLOAT.textify(printer, (FloatAnnotationValue) value);
        } else if (value instanceof DoubleAnnotationValue) {
            DOUBLE.textify(printer, (DoubleAnnotationValue) value);
        } else {
            throw new AssertionError();
        }
    };

    public final static Textifier<StringAnnotationValue> STRING = (printer, value) ->
            printer.addEscaped(value.getValue(), '"');

    public final static Textifier<ArrayAnnotationValue> ARRAY = (printer, value) -> {
        List<AnnotationValue> array = value.getArray();
        if (array.isEmpty()) {
            printer.addText("{}");
        } else {
            AnnotationValueTextifier valueTextifier = getInstance();
            printer.addText("{ ");

            TextUtil.joined(array,
                    element -> valueTextifier.textify(printer, element),
                    () -> printer.addText(", "));

            printer.addText(" }");
        }
    };

    public final static Textifier<ClassAnnotationValue> CLASS = (printer, value) -> {
        TypeTextifier.getInstance().textify(printer, value.getValue());
        printer.addText(".class");
    };

    public final static Textifier<EnumAnnotationValue> ENUM = (printer, value) -> {
        printer.addPath(value.getType());
        printer.addText(".");
        printer.addLiteral(value.getName());
    };

    public final static Textifier<AnnotationAnnotationValue> ANNOTATION = (printer, value) -> {
        printer.addText("@");
        printer.addPath(value.getType());
        printer.addText("(");
        AnnotationTextifier.appendValues(printer, value);
        printer.addText(")");
    };

    @Override
    public void textify(Printer printer, AnnotationValue value) {
        if (value instanceof PrimitiveAnnotationValue) {
            PRIMITIVE.textify(printer, (PrimitiveAnnotationValue) value);
        } else if (value instanceof StringAnnotationValue) {
            STRING.textify(printer, (StringAnnotationValue) value);
        } else if (value instanceof ArrayAnnotationValue) {
            ARRAY.textify(printer, (ArrayAnnotationValue) value);
        } else if (value instanceof AnnotationAnnotationValue) {
            ANNOTATION.textify(printer, (AnnotationAnnotationValue) value);
        } else if (value instanceof ClassAnnotationValue) {
            CLASS.textify(printer, (ClassAnnotationValue) value);
        } else if (value instanceof EnumAnnotationValue) {
            ENUM.textify(printer, (EnumAnnotationValue) value);
        } else {
            throw new AssertionError();
        }
    }
}
