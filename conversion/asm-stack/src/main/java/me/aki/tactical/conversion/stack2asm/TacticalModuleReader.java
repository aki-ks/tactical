package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.asmutils.AccessConverter;
import me.aki.tactical.core.Module;
import org.objectweb.asm.ModuleVisitor;

public class TacticalModuleReader {
    private final Module module;

    public TacticalModuleReader(Module module) {
        this.module = module;
    }

    public void accept(ModuleVisitor mv) {
        visitMainClass(mv);
        visitPackages(mv);
        visitRequires(mv);
        visitExports(mv);
        visitOpens(mv);
        visitUses(mv);
        visitProvides(mv);
        mv.visitEnd();
    }

    private void visitMainClass(ModuleVisitor mv) {
        module.getMainClass()
                .map(AsmUtil::toInternalName)
                .ifPresent(mv::visitMainClass);
    }

    private void visitPackages(ModuleVisitor mv) {
        module.getPackages().stream()
                .map(AsmUtil::toInternalName)
                .forEach(mv::visitPackage);
    }

    private void visitRequires(ModuleVisitor mv) {
        for (Module.Require require : module.getRequires()) {
            String name = AsmUtil.toModuleName(require.getName());
            int access = AccessConverter.MODULE_REQUIRE.toBitMap(require.getFlags());
            String version = require.getVersion().orElse(null);

            mv.visitRequire(name, access, version);
        }
    }

    private void visitExports(ModuleVisitor mv) {
        for (Module.Export export : module.getExports()) {
            String pkg = AsmUtil.toInternalName(export.getName());
            int access = AccessConverter.MODULE_EXPORT.toBitMap(export.getFlags());
            String[] modules = export.getModules().stream()
                    .map(AsmUtil::toModuleName)
                    .toArray(String[]::new);

            mv.visitExport(pkg, access, modules);
        }
    }

    private void visitOpens(ModuleVisitor mv) {
        for (Module.Open open : module.getOpens()) {
            String pkg = AsmUtil.toInternalName(open.getName());
            int access = AccessConverter.MODULE_OPEN.toBitMap(open.getFlags());
            String[] modules = open.getModules().stream()
                    .map(AsmUtil::toModuleName)
                    .toArray(String[]::new);

            mv.visitOpen(pkg, access, modules);
        }
    }

    private void visitUses(ModuleVisitor mv) {
        module.getUses().stream()
                .map(AsmUtil::toInternalName)
                .forEach(mv::visitUse);
    }

    private void visitProvides(ModuleVisitor mv) {
        for (Module.Provide provide : module.getProvides()) {
            String service = AsmUtil.toInternalName(provide.getService());
            String[] providers = provide.getProviders().stream()
                    .map(AsmUtil::toInternalName)
                    .toArray(String[]::new);

            mv.visitProvide(service, providers);
        }
    }
}
