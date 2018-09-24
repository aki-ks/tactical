package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stackasm.AccessConverter;
import me.aki.tactical.core.Method;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.annotation.AnnotationValue;
import me.aki.tactical.core.typeannotation.MethodTypeAnnotation;
import me.aki.tactical.core.typeannotation.TargetType;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

import java.util.LinkedHashMap;

public class TacticalMethodReader {
    private final Method method;

    public TacticalMethodReader(Method method) {
        this.method = method;
    }

    public void accept(MethodVisitor mv) {
        visitParameters(mv);
        visitAnnotationDefault(mv);
        visitAnnotations(mv);
        visitTypeAnnotations(mv);
        mv.visitEnd();
    }

    private void visitParameters(MethodVisitor mv) {
        for (Method.Parameter parameter : method.getParameterInfo()) {
            String name = parameter.getName().orElse(null);
            int access = AccessConverter.parameter.toBitMap(parameter.getFlags());

            mv.visitParameter(name, access);
        }
    }

    private void visitAnnotationDefault(MethodVisitor mv) {
        method.getDefaultValue().ifPresent(annoValue -> {
            AnnotationVisitor av = mv.visitAnnotationDefault();
            if (av != null) {
                LinkedHashMap<String, AnnotationValue> map = new LinkedHashMap<>();
                map.put(null, annoValue);
                new TacticalAnnotationReader(map);
            }
        });
    }

    private void visitAnnotations(MethodVisitor mv) {
        for (Annotation annotation : method.getAnnotations()) {
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());
            boolean isVisible = annotation.isRuntimeVisible();

            AnnotationVisitor av = mv.visitAnnotation(descriptor, isVisible);
            if (av != null) {
                new TacticalAnnotationReader(annotation.getValues()).accept(av);
            }
        }
    }

    private void visitTypeAnnotations(MethodVisitor cv) {
        for (MethodTypeAnnotation typeAnnotation : method.getTypeAnnotations()) {
            Annotation annotation = typeAnnotation.getAnnotation();

            int typeRef = convertTargetType(typeAnnotation.getTargetType()).getValue();
            TypePath typePath = AsmUtil.toAsmTypePath(typeAnnotation.getTypePath());
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());
            boolean isVisible = annotation.isRuntimeVisible();

            AnnotationVisitor av = cv.visitTypeAnnotation(typeRef, typePath, descriptor, isVisible);
            if (av != null) {
                new TacticalAnnotationReader(annotation.getValues()).accept(av);
            }
        }
    }

    private TypeReference convertTargetType(TargetType.MethodTargetType targetType) {
        if (targetType instanceof TargetType.CheckedException) {
            int exceptionIndex = ((TargetType.CheckedException) targetType).getException();
            return TypeReference.newExceptionReference(exceptionIndex);
        } else if (targetType instanceof TargetType.MethodParameter) {
            int parameterIndex = ((TargetType.MethodParameter) targetType).getParameter();
            return TypeReference.newFormalParameterReference(parameterIndex);
        } else if (targetType instanceof TargetType.MethodReceiver) {
            return TypeReference.newTypeReference(TypeReference.METHOD_RECEIVER);
        } else if (targetType instanceof TargetType.ReturnType) {
            return TypeReference.newTypeReference(TypeReference.METHOD_RETURN);
        } else if (targetType instanceof TargetType.TypeParameter) {
            int parameterIndex = ((TargetType.TypeParameter) targetType).getParameterIndex();
            return TypeReference.newTypeParameterReference(TypeReference.METHOD_TYPE_PARAMETER, parameterIndex);
        } else if (targetType instanceof TargetType.TypeParameterBound) {
            int parameterIndex = ((TargetType.TypeParameterBound) targetType).getParameterIndex();
            int boundIndex = ((TargetType.TypeParameterBound) targetType).getBoundIndex();
            return TypeReference.newTypeParameterBoundReference(TypeReference.METHOD_TYPE_PARAMETER_BOUND, parameterIndex, boundIndex);
        } else {
            throw new AssertionError();
        }
    }
}
