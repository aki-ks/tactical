package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.core.Field;
import me.aki.tactical.core.annotation.Annotation;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

public class TacticalFieldReader {
    private final Field field;

    public TacticalFieldReader(Field field) {
        this.field = field;
    }

    public void accept(FieldVisitor fv) {
        visitAnnotations(fv);
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
}
