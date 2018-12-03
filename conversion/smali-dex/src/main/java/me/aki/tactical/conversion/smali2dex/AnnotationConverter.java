package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.annotation.*;
import me.aki.tactical.core.type.Type;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.value.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for conversion of annotations.
 */
public class AnnotationConverter {
    public static class InconvertibleEnumValueException extends RuntimeException {
        public InconvertibleEnumValueException(String message) {
            super(message);
        }
    }

    /**
     * Try to convert a list of smali annotations into tactical annotations.
     * Annotations that cannot be represented in jvm bytecode are not converted.
     *
     * @param annotations the smali annotations to be converted
     * @return the successfully converted annotations
     */
    public static List<Annotation> convertAnnotations(Set<? extends org.jf.dexlib2.iface.Annotation> annotations) {
        List<Annotation> annos = new ArrayList<>();
        for (org.jf.dexlib2.iface.Annotation annotation : annotations) {
            try {
                annos.add(convertAnnotation(annotation));
            } catch (InconvertibleEnumValueException e) {
            }
        }
        return annos;
    }

    /**
     * Try to convert a smali annotation to a tactical annotation.
     *
     * @param smaliAnno smali annotation to be converted
     * @return the converted annotation
     * @throws InconvertibleEnumValueException the annotation is not representable in jvm bytecode.
     */
    public static Annotation convertAnnotation(org.jf.dexlib2.iface.Annotation smaliAnno) throws InconvertibleEnumValueException {
        Path type = DexUtils.parseObjectDescriptor(smaliAnno.getType());

        boolean isRuntimeVisible = smaliAnno.getVisibility() == AnnotationVisibility.RUNTIME;

        LinkedHashMap<String, AnnotationValue> values = new LinkedHashMap<>();
        for (AnnotationElement element : smaliAnno.getElements()) {
            values.put(element.getName(), convertAnnotationValue(element.getValue()));
        }

        return new Annotation(type, isRuntimeVisible, values);
    }

    private static AnnotationValue convertAnnotationValue(EncodedValue value) throws InconvertibleEnumValueException {
        switch (value.getValueType()) {
            case ValueType.BOOLEAN: return new BooleanAnnotationValue(((BooleanEncodedValue) value).getValue());
            case ValueType.BYTE: return new ByteAnnotationValue(((ByteEncodedValue) value).getValue());
            case ValueType.SHORT: return new ShortAnnotationValue(((ShortEncodedValue) value).getValue());
            case ValueType.CHAR: return new CharAnnotationValue(((CharEncodedValue) value).getValue());
            case ValueType.INT: return new IntAnnotationValue(((IntEncodedValue) value).getValue());
            case ValueType.LONG: return new LongAnnotationValue(((LongEncodedValue) value).getValue());
            case ValueType.FLOAT: return new FloatAnnotationValue(((FloatEncodedValue) value).getValue());
            case ValueType.DOUBLE: return new DoubleAnnotationValue(((DoubleEncodedValue) value).getValue());

            case ValueType.STRING:
                return new StringAnnotationValue(((StringEncodedValue) value).getValue());

            case ValueType.ENUM:
                FieldReference enumConstant = ((EnumEncodedValue) value).getValue();
                return new EnumAnnotationValue(DexUtils.parseObjectDescriptor(enumConstant.getType()), enumConstant.getName());

            case ValueType.TYPE: {
                Type type = DexUtils.parseDescriptor(((TypeEncodedValue) value).getValue());
                return new ClassAnnotationValue(type);
            }

            case ValueType.ARRAY: {
                List<? extends EncodedValue> values = ((ArrayEncodedValue) value).getValue();
                return new ArrayAnnotationValue(values.stream().map(AnnotationConverter::convertAnnotationValue).collect(Collectors.toList()));
            }

            case ValueType.ANNOTATION: {
                AnnotationEncodedValue annotation = (AnnotationEncodedValue) value;
                Path type = DexUtils.parseObjectDescriptor(annotation.getType());
                LinkedHashMap<String, AnnotationValue> values = new LinkedHashMap<>();
                for (AnnotationElement element : annotation.getElements()) {
                    values.put(element.getName(), convertAnnotationValue(element.getValue()));
                }
                return new AnnotationAnnotationValue(type, values);
            }

            case ValueType.METHOD_TYPE: throw new InconvertibleEnumValueException("Method type");
            case ValueType.METHOD_HANDLE: throw new InconvertibleEnumValueException("Method handle");
            case ValueType.FIELD: throw new InconvertibleEnumValueException("Field reference");
            case ValueType.METHOD: throw new InconvertibleEnumValueException("Method reference");
            case ValueType.NULL: throw new InconvertibleEnumValueException("Null value");
            default: throw new AssertionError(value.getValueType());
        }
    }
}
