package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.annotation.*;
import me.aki.tactical.core.type.Type;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.ImmutableAnnotation;
import org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.value.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility for conversion of tactical annotations to smali annotations.
 */
public class AnnotationConverter {
    public static Set<? extends Annotation> convertAnnotations(List<me.aki.tactical.core.annotation.Annotation> annotations) {
        return annotations.stream()
                .map(AnnotationConverter::convertAnnotation)
                .collect(Collectors.toSet());
    }

    public static Annotation convertAnnotation(me.aki.tactical.core.annotation.Annotation annotation) {
        int visibility = annotation.isRuntimeVisible() ? AnnotationVisibility.RUNTIME : AnnotationVisibility.BUILD;
        String type = DexUtils.toObjectDescriptor(annotation.getType());
        Set<ImmutableAnnotationElement> values = annotation.getValues().entrySet().stream()
                .map(e -> new ImmutableAnnotationElement(e.getKey(), convertAnnotationValue(e.getValue())))
                .collect(Collectors.toSet());

        return new ImmutableAnnotation(visibility, type, values);
    }

    public static EncodedValue convertAnnotationValue(AnnotationValue av) {
        if (av instanceof PrimitiveAnnotationValue) {
            if (av instanceof BooleanAnnotationValue) {
                return ImmutableBooleanEncodedValue.forBoolean(((BooleanAnnotationValue) av).getValue());
            } else if (av instanceof ByteAnnotationValue) {
                return new ImmutableByteEncodedValue(((ByteAnnotationValue) av).getValue());
            } else if (av instanceof CharAnnotationValue) {
                return new ImmutableCharEncodedValue(((CharAnnotationValue) av).getValue());
            } else if (av instanceof ShortAnnotationValue) {
                return new ImmutableShortEncodedValue(((ShortAnnotationValue) av).getValue());
            } else if (av instanceof IntAnnotationValue) {
                return new ImmutableIntEncodedValue(((IntAnnotationValue) av).getValue());
            } else if (av instanceof LongAnnotationValue) {
                return new ImmutableLongEncodedValue(((LongAnnotationValue) av).getValue());
            } else if (av instanceof FloatAnnotationValue) {
                return new ImmutableFloatEncodedValue(((FloatAnnotationValue) av).getValue());
            } else if (av instanceof DoubleAnnotationValue) {
                return new ImmutableDoubleEncodedValue(((DoubleAnnotationValue) av).getValue());
            } else {
                throw new AssertionError();
            }
        } else if (av instanceof StringAnnotationValue) {
            return new ImmutableStringEncodedValue(((StringAnnotationValue) av).getValue());
        } else if (av instanceof ClassAnnotationValue) {
            Type type = ((ClassAnnotationValue) av).getValue();
            return new ImmutableTypeEncodedValue(DexUtils.toDexType(type));
        } else if (av instanceof ArrayAnnotationValue) {
            List<EncodedValue> values = ((ArrayAnnotationValue) av).getArray().stream()
                    .map(AnnotationConverter::convertAnnotationValue)
                    .collect(Collectors.toList());
            return new ImmutableArrayEncodedValue(values);
        } else if (av instanceof EnumAnnotationValue) {
            EnumAnnotationValue enumAnnotationValue = (EnumAnnotationValue) av;
            String name = enumAnnotationValue.getName();
            String type = DexUtils.toObjectDescriptor(enumAnnotationValue.getType());
            String definingClass = type; // enum constants should always be defined in the enum class
            return new ImmutableEnumEncodedValue(new ImmutableFieldReference(definingClass, name, type));
        } else if (av instanceof AnnotationAnnotationValue) {
            AnnotationAnnotationValue aav = (AnnotationAnnotationValue) av;
            String type = DexUtils.toObjectDescriptor(aav.getType());

            Set<ImmutableAnnotationElement> values = aav.getValues().entrySet().stream()
                    .map(e -> new ImmutableAnnotationElement(e.getKey(), convertAnnotationValue(e.getValue())))
                    .collect(Collectors.toSet());

            return new ImmutableAnnotationEncodedValue(type, values);
        } else {
            throw new AssertionError();
        }
    }
}
