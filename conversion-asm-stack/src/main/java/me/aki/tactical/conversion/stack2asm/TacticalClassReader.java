package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stackasm.AccessConverter;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.annotation.Annotation;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;

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
}
