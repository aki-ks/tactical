package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.conversion.stackasm.AsmUtil;
import me.aki.tactical.core.Path;
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
import me.aki.tactical.core.annotation.ShortAnnotationValue;
import me.aki.tactical.core.annotation.StringAnnotationValue;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * AnnotationVisitor that converts all asm events to {@link AnnotationValue} representations.
 */
public abstract class AbstractAnnotationConvertVisitor extends AnnotationVisitor {
    public AbstractAnnotationConvertVisitor(AnnotationVisitor annotationVisitor) {
        super(Opcodes.ASM6, annotationVisitor);
    }

    /**
     * Visits an {@link AnnotationValue} build from an asm event.
     *
     * @param name of the method or {@code null} while visiting an array
     * @param value converted annotation value
     */
    public abstract void visitConvertedAnnotationValue(String name, AnnotationValue value);

    @Override
    public void visit(String name, Object value) {
        super.visit(name, value);

        visitConvertedAnnotationValue(name, convertAnnotationValue(value));
    }

    private AnnotationValue convertAnnotationValue(Object value) {
        if (value instanceof Number) {
            if (value instanceof Byte) {
                return new ByteAnnotationValue((Byte) value);
            } else if (value instanceof Short) {
                return new ShortAnnotationValue((Short) value);
            } else if (value instanceof Integer) {
                return new IntAnnotationValue((Integer) value);
            } else if (value instanceof Long) {
                return new LongAnnotationValue((Long) value);
            } else if (value instanceof Float) {
                return new FloatAnnotationValue((Float) value);
            } else if (value instanceof Double) {
                return new DoubleAnnotationValue((Double) value);
            }
        } else if (value.getClass().isArray()) {
            List<AnnotationValue> array = new ArrayList<>(Array.getLength(value));

            if (value instanceof boolean[]) {
                for (boolean b : (boolean[]) value)
                    array.add(new BooleanAnnotationValue(b));
            } else if (value instanceof byte[]) {
                for (byte b : (byte[]) value)
                    array.add(new ByteAnnotationValue(b));
            } else if (value instanceof char[]) {
                for (char c : (char[]) value)
                    array.add(new CharAnnotationValue(c));
            } else if (value instanceof short[]) {
                for (short s : (short[]) value)
                    array.add(new ShortAnnotationValue(s));
            } else if (value instanceof int[]) {
                for (int i : (int[]) value)
                    array.add(new IntAnnotationValue(i));
            } else if (value instanceof long[]) {
                for (long l : (long[]) value)
                    array.add(new LongAnnotationValue(l));
            } else if (value instanceof float[]) {
                for (float f : (float[]) value)
                    array.add(new FloatAnnotationValue(f));
            } else if (value instanceof double[]) {
                for (double d : (double[]) value)
                    array.add(new DoubleAnnotationValue(d));
            } else {
                throw new IllegalArgumentException("Arrays of reference type are not allowed");
            }

            return new ArrayAnnotationValue(array);
        } else if (value instanceof String) {
            return new StringAnnotationValue((String) value);
        } else if (value instanceof Boolean) {
            return new BooleanAnnotationValue((Boolean) value);
        } else if (value instanceof Character) {
            return new CharAnnotationValue((Character) value);
        } else if (value instanceof org.objectweb.asm.Type) {
            org.objectweb.asm.Type type = (org.objectweb.asm.Type) value;
            return new ClassAnnotationValue(AsmUtil.fromAsmType(type));
        }

        throw new IllegalArgumentException("Cannot convert value of type " + value.getClass().getName());
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        super.visitEnum(name, descriptor, value);

        Path owner = AsmUtil.pathFromObjectDescriptor(descriptor);
        visitConvertedAnnotationValue(name, new EnumAnnotationValue(owner, value));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        AnnotationAnnotationValue annotation = new AnnotationAnnotationValue(AsmUtil.pathFromObjectDescriptor(descriptor));

        AnnotationVisitor av = super.visitAnnotation(name, descriptor);
        av = new AnnotationConvertVisitor(av, annotation) {
            @Override
            public void visitEnd() {
                super.visitEnd();

                AbstractAnnotationConvertVisitor.this.visitConvertedAnnotationValue(name, annotation);
            }
        };
        return av;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        ArrayAnnotationValue annotationValue = new ArrayAnnotationValue();

        AnnotationVisitor av = super.visitArray(name);
        av = new AbstractAnnotationConvertVisitor(av) {
            @Override
            public void visitConvertedAnnotationValue(String name, AnnotationValue av) {
                annotationValue.getArray().add(av);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();

                AbstractAnnotationConvertVisitor.this.visitConvertedAnnotationValue(name, annotationValue);
            }
        };
        return av;
    }
}
