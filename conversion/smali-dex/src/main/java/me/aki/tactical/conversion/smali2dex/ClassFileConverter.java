package me.aki.tactical.conversion.smali2dex;

import me.aki.tactical.conversion.smalidex.FlagConverter;
import me.aki.tactical.conversion.smalidex.DexUtils;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Field;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.constant.DoubleConstant;
import me.aki.tactical.core.constant.FieldConstant;
import me.aki.tactical.core.constant.FloatConstant;
import me.aki.tactical.core.constant.IntConstant;
import me.aki.tactical.core.constant.LongConstant;
import me.aki.tactical.core.constant.StringConstant;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.utils.VersionVisitor;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.value.BooleanEncodedValue;
import org.jf.dexlib2.iface.value.ByteEncodedValue;
import org.jf.dexlib2.iface.value.CharEncodedValue;
import org.jf.dexlib2.iface.value.DoubleEncodedValue;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.FloatEncodedValue;
import org.jf.dexlib2.iface.value.IntEncodedValue;
import org.jf.dexlib2.iface.value.LongEncodedValue;
import org.jf.dexlib2.iface.value.ShortEncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;

import java.util.*;
import java.util.stream.Collectors;

public class ClassFileConverter {
    private final ClassDef smaliClass;
    private final Classfile tacticalClass;

    public ClassFileConverter(ClassDef smaliClass) {
        this.smaliClass = smaliClass;

        this.tacticalClass = convertClassDescriptor();
        this.tacticalClass.setFlags(FlagConverter.CLASSFILE.fromBitMap(smaliClass.getAccessFlags()));
        this.tacticalClass.setSource(Optional.ofNullable(smaliClass.getSourceFile()));
        this.tacticalClass.setAnnotations(AnnotationConverter.convertAnnotations(smaliClass.getAnnotations()));

        convertFields();
        convertMethods();

        this.tacticalClass.setVersion(computeJvmVersion());
    }

    private Classfile convertClassDescriptor() {
        Path name = DexUtils.parseObjectDescriptor(smaliClass.getType());

        String superDescriptor = smaliClass.getSuperclass();
        Path supertype = superDescriptor == null ? null : DexUtils.parseObjectDescriptor(superDescriptor);

        List<Path> interfaces = smaliClass.getInterfaces().stream()
                .map(DexUtils::parseObjectDescriptor)
                .collect(Collectors.toList());

        return new Classfile(null, name, supertype, interfaces);
    }

    /**
     * Compute the smallest possible jvm version for this classfile.
     *
     * @return the computed jvm version
     */
    private Classfile.Version computeJvmVersion() {
        VersionVisitor.JvmVersionCompute jvmVersionCompute = new VersionVisitor.JvmVersionCompute();
        VersionVisitor.accept(jvmVersionCompute, tacticalClass);
        return jvmVersionCompute.getVersion();
    }

    private void convertFields() {
        for (org.jf.dexlib2.iface.Field smaliField : smaliClass.getFields()) {
            Type type = DexUtils.parseDescriptor(smaliField.getType());
            Field field = new Field(smaliField.getName(), type);
            field.setFlags(FlagConverter.FIELD.fromBitMap(smaliField.getAccessFlags()));
            field.setAnnotations(AnnotationConverter.convertAnnotations(smaliField.getAnnotations()));
            field.setValue(Optional.ofNullable(smaliField.getInitialValue()).map(this::convertFieldConstant));
            tacticalClass.getFields().add(field);
        }
    }

    private FieldConstant convertFieldConstant(EncodedValue value) {
        switch (value.getValueType()) {
            case ValueType.BOOLEAN: return new IntConstant(((BooleanEncodedValue) value).getValue() ? 1 : 0);
            case ValueType.BYTE: return new IntConstant(((ByteEncodedValue) value).getValue());
            case ValueType.SHORT: return new IntConstant(((ShortEncodedValue) value).getValue());
            case ValueType.CHAR: return new IntConstant(((CharEncodedValue) value).getValue());
            case ValueType.INT: return new IntConstant(((IntEncodedValue) value).getValue());
            case ValueType.LONG: return new LongConstant(((LongEncodedValue) value).getValue());
            case ValueType.FLOAT: return new FloatConstant(((FloatEncodedValue) value).getValue());
            case ValueType.DOUBLE: return new DoubleConstant(((DoubleEncodedValue) value).getValue());
            case ValueType.STRING: return new StringConstant(((StringEncodedValue) value).getValue());
            default: throw new AssertionError(value);
        }
    }

    private void convertMethods() {
        for (org.jf.dexlib2.iface.Method smaliMethod : smaliClass.getMethods()) {
            List<Type> paramTypes = smaliMethod.getParameterTypes().stream()
                    .map(DexUtils::parseDescriptor).collect(Collectors.toList());
            Optional<Type> returnType = DexUtils.parseReturnType(smaliMethod.getReturnType());
            Method method = new Method(smaliMethod.getName(), paramTypes, returnType);

            method.setParameterAnnotations(smaliMethod.getParameters().stream()
                    .map(param -> AnnotationConverter.convertAnnotations(param.getAnnotations()))
                    .collect(Collectors.toList()));

            method.setParameterInfo(smaliMethod.getParameters().stream()
                    .map(param -> new Method.Parameter(Optional.ofNullable(param.getName()), new HashSet<>()))
                    .collect(Collectors.toList()));

            method.setFlags(FlagConverter.METHOD.fromBitMap(smaliMethod.getAccessFlags()));
            method.setAnnotations(AnnotationConverter.convertAnnotations(smaliMethod.getAnnotations()));
            method.setBody(Optional.ofNullable(smaliMethod.getImplementation())
                    .map(body -> new BodyConverter(method, body).getBody()));
            tacticalClass.getMethods().add(method);
        }
    }

    public Classfile getTacticalClass() {
        return tacticalClass;
    }
}
