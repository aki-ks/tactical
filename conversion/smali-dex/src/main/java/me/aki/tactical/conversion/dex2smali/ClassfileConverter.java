package me.aki.tactical.conversion.dex2smali;

import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.conversion.smalidex.FlagConverter;
import me.aki.tactical.core.Body;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Field;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.constant.*;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.dex.DexBody;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.value.*;

import java.util.*;

public class ClassfileConverter {
    private final Classfile classfile;

    public ClassfileConverter(Classfile classfile) {
        this.classfile = classfile;
    }

    private org.jf.dexlib2.iface.Field convertField(Field field) {
        String definingClass = DexUtils.toObjectDescriptor(classfile.getName());
        String type = DexUtils.toDexType(field.getType());
        int accessFlags = FlagConverter.FIELD.toBitMap(field.getFlags());
        EncodedValue initialValue = field.getValue().map(this::convertFieldConstant).orElse(null);
        Set<? extends org.jf.dexlib2.iface.Annotation> annotations = AnnotationConverter.convertAnnotations(field.getAnnotations());
        return new ImmutableField(definingClass, field.getName(), type, accessFlags, initialValue, annotations);
    }

    private EncodedValue convertFieldConstant(FieldConstant constant) {
        if (constant instanceof IntConstant) {
            return new ImmutableIntEncodedValue(((IntConstant) constant).getValue());
        } else if (constant instanceof LongConstant) {
            return new ImmutableLongEncodedValue(((LongConstant) constant).getValue());
        } else if (constant instanceof FloatConstant) {
            return new ImmutableFloatEncodedValue(((FloatConstant) constant).getValue());
        } else if (constant instanceof DoubleConstant) {
            return new ImmutableDoubleEncodedValue(((DoubleConstant) constant).getValue());
        } else if (constant instanceof StringConstant) {
            return new ImmutableStringEncodedValue(((StringConstant) constant).getValue());
        } else {
            throw new AssertionError();
        }
    }

    private ImmutableMethod convertMethod(Method method) {
        String definingClass = DexUtils.toObjectDescriptor(classfile.getName());
        List<? extends MethodParameter> parameters = convertMethodParameters(method);
        String returnType = DexUtils.toDexReturnType(method.getReturnType());
        int accessFlags = FlagConverter.METHOD.toBitMap(method.getFlags());
        Set<? extends org.jf.dexlib2.iface.Annotation> annotations = AnnotationConverter.convertAnnotations(method.getAnnotations());
        MethodImplementation methodImplementation = method.getBody().map(this::convertMethodBody).orElse(null);
        return new ImmutableMethod(definingClass, method.getName(), parameters, returnType, accessFlags, annotations, methodImplementation);
    }

    private List<? extends MethodParameter> convertMethodParameters(Method method) {
        Iterator<Method.Parameter> paramInfo = method.getParameterInfo().iterator();
        Iterator<List<Annotation>> paramAnnotations = method.getParameterAnnotations().iterator();

        List<ImmutableMethodParameter> params = new ArrayList<>();
        for (Type parameterType : method.getParameterTypes()) {
            Optional<Method.Parameter> parameterOpt = paramInfo.hasNext() ? Optional.of(paramInfo.next()) : Optional.empty();
            String name = parameterOpt.flatMap(Method.Parameter::getName).orElse(null);

            List<Annotation> annotations = paramAnnotations.hasNext() ? paramAnnotations.next() : List.of();
            Set<? extends org.jf.dexlib2.iface.Annotation> smaliAnnotations = AnnotationConverter.convertAnnotations(annotations);

            params.add(new ImmutableMethodParameter(DexUtils.toDexType(parameterType), smaliAnnotations, name));
        }
        return params;
    }

    private MethodImplementation convertMethodBody(Body body) {
        if (body instanceof DexBody) {
            DexBody dexBody = (DexBody) body;

            throw new RuntimeException("NOT YET IMPLEMENTED");
        } else {
            throw new IllegalStateException("Expected DexBody, found " + body.getClass().getSimpleName());
        }
    }
}
