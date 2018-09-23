package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stackasm.AccessConverter;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Field;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.constant.DoubleConstant;
import me.aki.tactical.core.constant.FieldConstant;
import me.aki.tactical.core.constant.FloatConstant;
import me.aki.tactical.core.constant.IntConstant;
import me.aki.tactical.core.constant.LongConstant;
import me.aki.tactical.core.constant.StringConstant;
import me.aki.tactical.core.typeannotation.ClassTypeAnnotation;
import me.aki.tactical.core.typeannotation.TargetType;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

import java.util.Optional;

/**
 * Calls events on a {@link ClassVisitor} that generate bytecode for a {@link Classfile}.
 */
public class TacticalClassReader {
    private final Classfile classfile;

    public TacticalClassReader(Classfile classfile) {
        this.classfile = classfile;
    }

    public void accept(ClassVisitor cv) {
        visit(cv);
        visitSource(cv);
        visitModule(cv);
        visitOuterClass(cv);
        visitAnnotations(cv);
        visitTypeAnnotations(cv);
        //TODO: visitAttributes(cv);
        visitInnerClasses(cv);
        visitFields(cv);
        cv.visitEnd();
    }

    private void visit(ClassVisitor cv) {
        int version = convertVersion(classfile.getVersion());
        int access = AccessConverter.classfile.toBitMap(classfile.getAccessFlags());
        String name = classfile.getName().join('/');
        String signature = classfile.getSignature().orElse(null);
        String superName = classfile.getSupertype() == null ? null :
                classfile.getSupertype().join('/');
        String[] interfaces = classfile.getInterfaces().stream()
                .map(iface -> iface.join('/'))
                .toArray(String[]::new);

        cv.visit(version, access, name, signature, superName, interfaces);
    }

    private int convertVersion(Classfile.Version version) {
        return version.getMajor() | (version.getMinor() << 16);
    }

    private void visitSource(ClassVisitor cv) {
        Optional<String> source = classfile.getSource();
        Optional<String> debug = classfile.getSourceDebug();

        if (source.isPresent() || debug.isPresent()) {
            cv.visitSource(source.orElse(null), debug.orElse(null));
        }
    }

    private void visitModule(ClassVisitor cv) {
        classfile.getModule().ifPresent(module -> {
            String name = module.getModule().join('.');
            int access = AccessConverter.module.toBitMap(module.getAccessFlags());
            String version = module.getVersion().orElse(null);

            ModuleVisitor mv = cv.visitModule(name, access, version);
            if (mv != null) {
                new TacticalModuleReader(module).accept(mv);
            }
        });
    }

    private void visitOuterClass(ClassVisitor cv) {
        classfile.getEnclosingMethod().ifPresent(enclosingMethod -> {
            String owner = enclosingMethod.getOwner().join('/');
            String name = enclosingMethod.getName().orElse(null);
            String descriptor = enclosingMethod.getDescriptor()
                    .map(AsmUtil::methodDescriptorToString).orElse(null);

            cv.visitOuterClass(owner, name, descriptor);
        });
    }

    private void visitAnnotations(ClassVisitor cv) {
        for (Annotation annotation : classfile.getAnnotations()) {
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());
            boolean isVisible = annotation.isRuntimeVisible();

            AnnotationVisitor av = cv.visitAnnotation(descriptor, isVisible);
            if (av != null) {
                new TacticalAnnotationReader(annotation).accept(av);
            }
        }
    }

    private void visitTypeAnnotations(ClassVisitor cv) {
        for (ClassTypeAnnotation typeAnnotation : classfile.getTypeAnnotations()) {
            Annotation annotation = typeAnnotation.getAnnotation();

            int typeRef = convertTargetType(typeAnnotation.getTargetType()).getValue();
            TypePath typePath = AsmUtil.toAsmTypePath(typeAnnotation.getTypePath());
            String descriptor = AsmUtil.pathToDescriptor(annotation.getType());
            boolean isVisible = annotation.isRuntimeVisible();

            cv.visitTypeAnnotation(typeRef, typePath, descriptor, isVisible);
        }
    }

    private TypeReference convertTargetType(TargetType.ClassTargetType targetType) {
        if (targetType instanceof TargetType.Extends) {
            return TypeReference.newSuperTypeReference(-1);
        } else if (targetType instanceof TargetType.Implements) {
            int index = ((TargetType.Implements) targetType).getIndex();
            return TypeReference.newSuperTypeReference(index);
        } else if (targetType instanceof TargetType.TypeParameter) {
            int parameterIndex = ((TargetType.TypeParameter) targetType).getParameterIndex();
            return TypeReference.newTypeParameterReference(TypeReference.CLASS_TYPE_PARAMETER, parameterIndex);
        } else if (targetType instanceof TargetType.TypeParameterBound) {
            int parameterIndex = ((TargetType.TypeParameterBound) targetType).getParameterIndex();
            int boundIndex = ((TargetType.TypeParameterBound) targetType).getBoundIndex();
            return TypeReference.newTypeParameterBoundReference(TypeReference.CLASS_TYPE_PARAMETER_BOUND, parameterIndex, boundIndex);
        } else {
            throw new AssertionError();
        }
    }

    private void visitInnerClasses(ClassVisitor cv) {
        for (Classfile.InnerClass innerClass : classfile.getInnerClasses()) {
            String name = innerClass.getName().join('/');
            String outerName = innerClass.getOuterName().map(n -> n.join('/')).orElse(null);
            String innerName = innerClass.getInnerName().orElse(null);
            int access = AccessConverter.innerClass.toBitMap(innerClass.getFlags());

            cv.visitInnerClass(name, outerName, innerName, access);
        }
    }

    private void visitFields(ClassVisitor cv) {
        for (Field field : this.classfile.getFields()) {
            int access = AccessConverter.field.toBitMap(field.getAccessFlags());
            String name = field.getName();
            String descriptor = AsmUtil.toDescriptor(field.getType());
            String signature = field.getSignature().orElse(null);
            Object value = field.getValue().map(this::convertFieldConstant).orElse(null);

            FieldVisitor fv = cv.visitField(access, name, descriptor, signature, value);
            if (fv != null) {
                new TacticalFieldReader(field).accept(fv);
            }
        }
    }

    private Object convertFieldConstant(FieldConstant constant) {
        if (constant instanceof StringConstant) {
            return ((StringConstant) constant).getValue();
        } else if (constant instanceof IntConstant) {
            return ((IntConstant) constant).getValue();
        } else if (constant instanceof LongConstant) {
            return ((LongConstant) constant).getValue();
        } else if (constant instanceof FloatConstant) {
            return ((FloatConstant) constant).getValue();
        } else if (constant instanceof DoubleConstant) {
            return ((DoubleConstant) constant).getValue();
        } else {
            throw new AssertionError();
        }
    }
}
