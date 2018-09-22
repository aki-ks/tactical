package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stackasm.AccessConverter;
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
                .map(main -> main.join('/'))
                .ifPresent(mv::visitMainClass);
    }

    private void visitPackages(ModuleVisitor mv) {
        module.getPackages().stream()
                .map(pkg -> pkg.join('/'))
                .forEach(mv::visitPackage);
    }

    private void visitRequires(ModuleVisitor mv) {
        for (Module.Require require : module.getRequires()) {
            String name = require.getName().join('.');
            int access = AccessConverter.moduleRequire.toBitMap(require.getFlags());
            String version = require.getVersion().orElse(null);

            mv.visitRequire(name, access, version);
        }
    }

    private void visitExports(ModuleVisitor mv) {
        for (Module.Export export : module.getExports()) {
            String pkg = export.getName().join('/');
            int access = AccessConverter.moduleExport.toBitMap(export.getFlags());
            String[] modules = export.getModules().stream()
                    .map(module -> module.join('.'))
                    .toArray(String[]::new);

            mv.visitExport(pkg, access, modules);
        }
    }

    private void visitOpens(ModuleVisitor mv) {
        for (Module.Open open : module.getOpens()) {
            String pkg = open.getName().join('/');
            int access = AccessConverter.moduleOpen.toBitMap(open.getFlags());
            String[] modules = open.getModules().stream()
                    .map(module -> module.join('.'))
                    .toArray(String[]::new);

            mv.visitOpen(pkg, access, modules);
        }
    }

    private void visitUses(ModuleVisitor mv) {
        module.getUses().stream()
                .map(path -> path.join('/'))
                .forEach(mv::visitUse);
    }

    private void visitProvides(ModuleVisitor mv) {
        for (Module.Provide provide : module.getProvides()) {
            String service = provide.getService().join('/');
            String[] providers = provide.getProviders().stream()
                    .map(path -> path.join('/'))
                    .toArray(String[]::new);

            mv.visitProvide(service, providers);
        }
    }
}
