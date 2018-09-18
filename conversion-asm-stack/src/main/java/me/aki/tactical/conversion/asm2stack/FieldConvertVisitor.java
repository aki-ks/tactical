package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.Field;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

public class FieldConvertVisitor extends FieldVisitor {
    private final Field field;

    public FieldConvertVisitor(FieldVisitor fieldVisitor, Field field) {
        super(Opcodes.ASM6, fieldVisitor);
        this.field = field;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
//        return super.visitAnnotation(descriptor, visible);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
//        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
        throw new RuntimeException("Not yet implemented");
    }
}
