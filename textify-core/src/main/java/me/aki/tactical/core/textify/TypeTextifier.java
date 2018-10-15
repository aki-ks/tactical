package me.aki.tactical.core.textify;

import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.BooleanType;
import me.aki.tactical.core.type.ByteType;
import me.aki.tactical.core.type.CharType;
import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.ShortType;
import me.aki.tactical.core.type.Type;

public class TypeTextifier implements Textifier<Type> {
    private static final TypeTextifier INSTANCE = new TypeTextifier();
    public static TypeTextifier getInstance() {
        return INSTANCE;
    }

    public static final Textifier<BooleanType> BOOLEAN = (printer, type) -> printer.addText("boolean");
    public static final Textifier<ByteType> BYTE = (printer, type) -> printer.addText("byte");
    public static final Textifier<ShortType> SHORT = (printer, type) -> printer.addText("short");
    public static final Textifier<CharType> CHAR = (printer, type) -> printer.addText("char");
    public static final Textifier<IntType> INT = (printer, type) -> printer.addText("int");
    public static final Textifier<LongType> LONG = (printer, type) -> printer.addText("long");
    public static final Textifier<FloatType> FLOAT = (printer, type) -> printer.addText("float");
    public static final Textifier<DoubleType> DOUBLE = (printer, type) -> printer.addText("double");

    public static final Textifier<ObjectType> OBJECT = (printer, object) -> printer.addPath(object.getName());

    public static final Textifier<ArrayType> ARRAY = (printer, array) -> {
        int dimensions = array.getDimensions();
        for (int i = 0; i < dimensions; i++) {
            printer.addText("[]");
        }

        TypeTextifier.getInstance().textify(printer, array.getBaseType());
    };

    public static final Textifier<IntLikeType> INT_LIKE = (printer, type) -> {
        if (type instanceof BooleanType) {
            BOOLEAN.textify(printer, (BooleanType) type);
        } else if (type instanceof ByteType) {
            BYTE.textify(printer, (ByteType) type);
        } else if (type instanceof CharType) {
            CHAR.textify(printer, (CharType) type);
        } else if (type instanceof IntType) {
            INT.textify(printer, (IntType) type);
        } else if (type instanceof ShortType) {
            SHORT.textify(printer, (ShortType) type);
        } else {
            throw new AssertionError();
        }
    };

    public static final Textifier<PrimitiveType> PRIMITIVE_TYPE = (printer, type) -> {
        if (type instanceof IntLikeType) {
            INT_LIKE.textify(printer, (IntLikeType) type);
        } else if (type instanceof LongType) {
            LONG.textify(printer, (LongType) type);
        } else if (type instanceof FloatType) {
            FLOAT.textify(printer, (FloatType) type);
        } else if (type instanceof DoubleType) {
            DOUBLE.textify(printer, (DoubleType) type);
        } else {
            throw new AssertionError();
        }
    };

    public static final Textifier<RefType> REF_TYPE = (printer, type) -> {
        if (type instanceof ObjectType) {
            OBJECT.textify(printer, (ObjectType) type);
        } else if (type instanceof ArrayType) {
            ARRAY.textify(printer, (ArrayType) type);
        }
    };

    /**
     * A special type textifier that textifies any reference type as <tt>ref</tt>.
     */
    public static final Textifier<Type> INSN_TYPE = (printer, type) -> {
        if (type instanceof RefType) {
            printer.addText("ref");
        } else {
            getInstance().textify(printer, type);
        }
    };

    private TypeTextifier() {}

    @Override
    public void textify(Printer printer, Type type) {
        if (type instanceof PrimitiveType) {
            PRIMITIVE_TYPE.textify(printer, (PrimitiveType) type);
        } else if (type instanceof RefType) {
            REF_TYPE.textify(printer, (RefType) type);
        } else {
            throw new AssertionError();
        }
    }
}
