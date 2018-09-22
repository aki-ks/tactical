package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stackasm.AccessConverter;
import me.aki.tactical.core.Classfile;
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
        doVisit(cv);
        doSourceVisit(cv);
        doVisitModule(cv);
        doOuterClassVisit(cv);
        cv.visitEnd();
    }

    private void doVisit(ClassVisitor cv) {
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

    private void doSourceVisit(ClassVisitor cv) {
        Optional<String> source = classfile.getSource();
        Optional<String> debug = classfile.getSourceDebug();

        if (source.isPresent() || debug.isPresent()) {
            cv.visitSource(source.orElse(null), debug.orElse(null));
        }
    }

    private void doVisitModule(ClassVisitor cv) {
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

    private void doOuterClassVisit(ClassVisitor cv) {
        classfile.getEnclosingMethod().ifPresent(enclosingMethod -> {
            String owner = enclosingMethod.getOwner().join('/');
            String name = enclosingMethod.getName().orElse(null);
            String descriptor = enclosingMethod.getDescriptor()
                    .map(AsmUtil::methodDescriptorToString).orElse(null);

            cv.visitOuterClass(owner, name, descriptor);
        });
    }
}
