package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.Method;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

/**
 * Visitor that converts all events and stores them in a {@link Method}.
 */
public class MethodConvertVisitor extends MethodVisitor {
    private Method method;

    public MethodConvertVisitor(MethodVisitor methodVisitor, Method method) {
        super(Opcodes.ASM6, methodVisitor);
        this.method = method;
    }

    @Override
    public void visitParameter(String name, int access) {
        super.visitParameter(name, access);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
//        return super.visitAnnotationDefault();
        throw new RuntimeException("Not yet implemented");
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
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        super.visitAnnotableParameterCount(parameterCount, visible);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
//        return super.visitParameterAnnotation(parameter, descriptor, visible);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitCode() {
        super.visitCode();
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        super.visitFrame(type, nLocal, local, nStack, stack);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, type);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(value);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
//        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
        throw new RuntimeException("Not yet implemented");
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

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        throw new RuntimeException("Not yet implemented");
    }
}
