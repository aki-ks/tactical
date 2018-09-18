package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.Module;
import me.aki.tactical.core.annotation.Annotation;
import me.aki.tactical.core.typeannotation.ClassTypeAnnotation;
import me.aki.tactical.core.typeannotation.TargetType;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link ClassVisitor} that creates a {@link Classfile} from the visited events.
 */
public class ClassConvertVisitor extends ClassVisitor {
    private Classfile classfile;

    public ClassConvertVisitor(int api) {
        super(api);
    }

    public Classfile getClassfile() {
        return classfile;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        Classfile.Version convertedVersion = parseVersion(version);

        Path convertedSuperType = superName == null ? null : AsmUtil.pathFromInternalName(superName);

        List<Path> convertedInterfaces = (interfaces == null ? Stream.<String>empty() : Arrays.stream(interfaces))
                .map(AsmUtil::pathFromInternalName)
                .collect(Collectors.toList());

        this.classfile = new Classfile(convertedVersion, AsmUtil.pathFromInternalName(name), convertedSuperType, convertedInterfaces);
        this.classfile.setSignature(Optional.ofNullable(signature));
        this.classfile.getAccessFlags().addAll(AccessConverter.classfile.fromBitMap(access));
    }

    private Classfile.Version parseVersion(int version) {
        int major = version & 0xFFFF;
        int minor = (version >>> 16) & 0xFFFF;
        return new Classfile.Version(major, minor);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);

        this.classfile.setSource(Optional.ofNullable(source));
        this.classfile.setSourceDebug(Optional.ofNullable(debug));
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        Module module = new Module(AsmUtil.pathFromModuleName(name));
        module.setVersion(Optional.ofNullable(version));
        module.getAccessFlags().addAll(AccessConverter.module.fromBitMap(access));
        this.classfile.setModule(Optional.of(module));

        ModuleVisitor mv = super.visitModule(name, access, version);
        mv = new ModuleConvertVisitor(module, mv);
        return mv;
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        super.visitOuterClass(owner, name, descriptor);
        Path ownerPath = AsmUtil.pathFromInternalName(owner);
        Optional<MethodDescriptor> methodDescOpt = Optional.ofNullable(descriptor).map(AsmUtil::parseMethodDescriptor);

        this.classfile.setEnclosingMethod(Optional.of(new Classfile.EnclosingMethod(ownerPath, Optional.ofNullable(name), methodDescOpt)));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Annotation annotation = new Annotation(AsmUtil.pathFromObjectDescriptor(descriptor), visible);
        this.classfile.getAnnotations().add(annotation);

        AnnotationVisitor av = super.visitAnnotation(descriptor, visible);
        av = new AnnotationConvertVisitor(av, annotation);
        return av;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        Annotation annotation = new Annotation(AsmUtil.pathFromObjectDescriptor(descriptor), visible);
        TargetType.ClassTargetType targetType = convertClassTargetType(typeRef);
        this.classfile.getTypeAnnotations().add(new ClassTypeAnnotation(AsmUtil.fromAsmTypePath(typePath), annotation, targetType));

        AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        av = new AnnotationConvertVisitor(av, annotation);
        return av;
    }

    private TargetType.ClassTargetType convertClassTargetType(int typeRef) {
        TypeReference tref = new TypeReference(typeRef);
        switch (tref.getSort()) {
            case TypeReference.CLASS_TYPE_PARAMETER:
                return new TargetType.TypeParameter(tref.getTypeParameterIndex());

            case TypeReference.CLASS_TYPE_PARAMETER_BOUND:
                return new TargetType.TypeParameterBound(tref.getTypeParameterIndex(), tref.getTypeParameterBoundIndex());

            case TypeReference.CLASS_EXTENDS:
                int interfaceIndex = tref.getSuperTypeIndex();
                return interfaceIndex == -1 ? new TargetType.Extends() :
                        new TargetType.Implements(interfaceIndex);

            default:
                throw new AssertionError();
        }
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);

        this.classfile.getInnerClasses().add(new Classfile.InnerClass(
                AsmUtil.pathFromInternalName(name),
                Optional.ofNullable(outerName).map(AsmUtil::pathFromInternalName),
                Optional.ofNullable(innerName),
                AccessConverter.innerClass.fromBitMap(access)));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
//        return super.visitField(access, name, descriptor, signature, value);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//        return super.visitMethod(access, name, descriptor, signature, exceptions);
        throw new RuntimeException("Not yet implemented");
    }
}
