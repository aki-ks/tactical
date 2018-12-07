package me.aki.tactical.dex.utils;

import me.aki.tactical.core.Body;
import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Method;
import me.aki.tactical.dex.DexBody;
import me.aki.tactical.dex.DexVersion;
import me.aki.tactical.dex.insn.InvokeInstruction;
import me.aki.tactical.dex.invoke.Invoke;
import me.aki.tactical.dex.invoke.InvokeCustom;
import me.aki.tactical.dex.invoke.InvokePolymorphic;
import me.aki.tactical.dex.insn.Instruction;

import java.util.Optional;

/**
 * Visit certain featured that were introduced with later vm versions.
 */
public class VersionVisitor {
    private final VersionVisitor vv;

    public VersionVisitor(VersionVisitor vv) {
        this.vv = vv;
    }

    public void visitStaticInterfaceMethod() {
        if (vv != null) {
            vv.visitStaticInterfaceMethod();
        }
    }

    public void visitDefaultInterfaceMethod() {
        if (vv != null) {
            vv.visitDefaultInterfaceMethod();
        }
    }

    public void visitInvokePolymorphic() {
        if (vv != null) {
            vv.visitInvokePolymorphic();
        }
    }

    public void visitInvokeDynamic() {
        if (vv != null) {
            vv.visitInvokeDynamic();
        }
    }

    public static void accept(VersionVisitor vv, Classfile... classfiles) {
        boolean foundStaticInterfaceMethod = false;
        boolean foundDefaultInterfaceMethod = false;
        boolean foundInvokePolymorphic = false;
        boolean foundInvokeDynamic = false;

        for (Classfile classfile : classfiles) {
            boolean isInterface = classfile.getFlag(Classfile.Flag.INTERFACE);

            for (Method method : classfile.getMethods()) {
                Optional<Body> bodyOpt = method.getBody();

                if (isInterface) {
                    if (!foundDefaultInterfaceMethod && bodyOpt.isPresent()) {
                        vv.visitDefaultInterfaceMethod();
                        foundDefaultInterfaceMethod = true;
                    }
                    if (!foundStaticInterfaceMethod && method.getFlag(Method.Flag.STATIC)) {
                        vv.visitStaticInterfaceMethod();
                        foundStaticInterfaceMethod = true;
                    }
                }

                if (bodyOpt.isPresent()) {
                    DexBody body = (DexBody) bodyOpt.get();
                    for (Instruction instruction : body.getInstructions()) {
                        if (instruction instanceof InvokeInstruction) {
                            Invoke invoke = ((InvokeInstruction) instruction).getInvoke();
                            if (!foundInvokePolymorphic && invoke instanceof InvokePolymorphic) {
                                vv.visitInvokePolymorphic();
                                foundInvokePolymorphic = true;
                            }
                            if (!foundInvokeDynamic && invoke instanceof InvokeCustom) {
                                vv.visitInvokeDynamic();
                                foundInvokeDynamic = true;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Computethe minimal required jvm version.
     */
    public static class JvmVersionCompute extends VersionVisitor {
        private int major = Classfile.Version.MAJOR_JDK_6;

        public JvmVersionCompute() {
            super(null);
        }

        public Classfile.Version getVersion() {
            return new Classfile.Version(major, 0);
        }

        @Override
        public void visitStaticInterfaceMethod() {
            this.major = Math.max(major, Classfile.Version.MAJOR_JDK_8);
        }

        @Override
        public void visitDefaultInterfaceMethod() {
            this.major = Math.max(this.major, Classfile.Version.MAJOR_JDK_8);
        }

        @Override
        public void visitInvokePolymorphic() {
            // Polymorphic method calls are only used for methods of the MethodHandle API
            // which was added in JDK 7.
            this.major = Math.max(this.major, Classfile.Version.MAJOR_JDK_7);
        }

        @Override
        public void visitInvokeDynamic() {
            this.major = Math.max(this.major, Classfile.Version.MAJOR_JDK_7);
        }
    }

    /**
     * Compute the minimal required dex version
     */
    public static class DexVersionCompute extends VersionVisitor {
        private int version = DexVersion.DEFAULT_VERSION;

        public DexVersionCompute() {
            super(null);
        }

        public int getVersion() {
            return version;
        }

        @Override
        public void visitStaticInterfaceMethod() {
            this.version = Math.max(this.version, DexVersion.API_24);
        }

        @Override
        public void visitDefaultInterfaceMethod() {
            this.version = Math.max(this.version, DexVersion.API_24);
        }

        @Override
        public void visitInvokePolymorphic() {
            this.version = Math.max(this.version, DexVersion.API_26);
        }

        @Override
        public void visitInvokeDynamic() {
            this.version = Math.max(this.version, DexVersion.API_26);
        }
    }
}
