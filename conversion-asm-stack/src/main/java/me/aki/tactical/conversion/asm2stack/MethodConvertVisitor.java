package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.annotation.AnnotationValue;
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation;
import me.aki.tactical.core.typeannotation.TargetType;
import me.aki.tactical.stack.StackBody;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Visitor that converts all events and stores them in a {@link Method}.
 */
public class MethodConvertVisitor extends JSRInlinerAdapter {
    private final Classfile classfile;
    private final Method method;
    private StackBody body;

    public MethodConvertVisitor(MethodVisitor methodVisitor, Classfile classfile, Method method, int access, String name, String descriptor, String signature, String[] exceptions) {
        super(Opcodes.ASM6, methodVisitor, access, name, descriptor, signature, exceptions);
        this.method = method;
        this.classfile = classfile;
    }

    @Override
    public void visitParameter(String name, int access) {
        super.visitParameter(name, access);

        Method.Parameter parameter = new Method.Parameter();
        parameter.setName(Optional.ofNullable(name));
        parameter.setFlags(AccessConverter.parameter.fromBitMap(access));

        this.method.getParameterInfo().add(parameter);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        AnnotationVisitor av = super.visitAnnotationDefault();
        av = new AbstractAnnotationConvertVisitor(av) {
            @Override
            public void visitConvertedAnnotationValue(String name, AnnotationValue value) {
                MethodConvertVisitor.this.method.setDefaultValue(Optional.of(value));
            }
        };
        return av;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Annotation annotation = new Annotation(AsmUtil.pathFromObjectDescriptor(descriptor), visible);
        this.method.getAnnotations().add(annotation);

        AnnotationVisitor av = super.visitAnnotation(descriptor, visible);
        av = new AnnotationConvertVisitor(av, annotation);
        return av;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        Annotation annotation = new Annotation(AsmUtil.pathFromObjectDescriptor(descriptor), visible);
        TargetType.MethodTargetType targetType = convertTargetType(new TypeReference(typeRef));
        this.method.getTypeAnnotations().add(new MethodTypeAnnotation(AsmUtil.fromAsmTypePath(typePath), annotation, targetType));

        AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        av = new AnnotationConvertVisitor(av, annotation);
        return av;
    }

    private TargetType.MethodTargetType convertTargetType(TypeReference tref) {
        switch (tref.getSort()) {
            case TypeReference.THROWS:
                return new TargetType.CheckedException(tref.getExceptionIndex());

            case TypeReference.METHOD_FORMAL_PARAMETER:
                return new TargetType.MethodParameter(tref.getFormalParameterIndex());

            case TypeReference.METHOD_RECEIVER:
                return new TargetType.MethodReceiver();

            case TypeReference.METHOD_RETURN:
                return new TargetType.ReturnType();

            case TypeReference.METHOD_TYPE_PARAMETER:
                return new TargetType.TypeParameter(tref.getTypeParameterIndex());

            case TypeReference.METHOD_TYPE_PARAMETER_BOUND:
                return new TargetType.TypeParameterBound(tref.getTypeParameterIndex(), tref.getTypeParameterBoundIndex());

            default:
                throw new AssertionError();
        }
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        List<List<Annotation>> parameterAnnotations = this.method.getParameterAnnotations();
        while (parameterAnnotations.size() <= parameter) {
            parameterAnnotations.add(new ArrayList<>());
        }

        Annotation annotation = new Annotation(AsmUtil.pathFromObjectDescriptor(descriptor), visible);
        parameterAnnotations.get(parameter).add(annotation);

        AnnotationVisitor av = super.visitParameterAnnotation(parameter, descriptor, visible);
        av = new AnnotationConvertVisitor(av, annotation);
        return av;
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitCode() {
        super.visitCode();

        this.body = new StackBody();
        this.method.setBody(Optional.of(this.body));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        if (this.body != null) {
            new BodyConverter(this.classfile, this.method, this.body, this).convert();
        }
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
//        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
//        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
        throw new RuntimeException("Not yet implemented");
    }
}
