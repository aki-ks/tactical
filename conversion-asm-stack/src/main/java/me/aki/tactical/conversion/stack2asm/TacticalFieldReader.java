package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.core.Attribute;
import me.aki.tactical.core.Field;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.typeannotation.FieldTypeAnnotation;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

public class TacticalFieldReader {
    private final Field field;

    public TacticalFieldReader(Field field) {
        this.field = field;
    }

    public void accept(FieldVisitor fv) {
        visitAnnotations(fv);
        visitTypeAnnotations(fv);
        visitAttributes(fv);
        fv.visitEnd();
    }

    private void visitAnnotations(FieldVisitor fv) {
        for (Annotation annotation : field.getAnnotations()) {
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());
            boolean isVisible = annotation.isRuntimeVisible();

            AnnotationVisitor av = fv.visitAnnotation(descriptor, isVisible);
            if (av != null) {
                new TacticalAnnotationReader(annotation).accept(av);
            }
        }
    }

    private void visitTypeAnnotations(FieldVisitor fv) {
        for (FieldTypeAnnotation typeAnnotation : field.getTypeAnnotations()) {
            Annotation annotation = typeAnnotation.getAnnotation();

            int typeRef = TypeReference.newTypeReference(TypeReference.FIELD).getValue();
            TypePath typePath = AsmUtil.toAsmTypePath(typeAnnotation.getTypePath());
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());
            boolean isVisible = annotation.isRuntimeVisible();

            AnnotationVisitor av = fv.visitTypeAnnotation(typeRef, typePath, descriptor, isVisible);
            if (av != null) {
                new TacticalAnnotationReader(annotation).accept(av);
            }
        }
    }

    private void visitAttributes(FieldVisitor fv) {
        for (Attribute attribute : field.getAttributes()) {
            fv.visitAttribute(new CustomAttribute(attribute.getName(), attribute.getData()));
        }
    }
}
