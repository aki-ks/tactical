package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.Module;
import me.aki.tactical.core.Path;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleConvertVisitor extends ModuleVisitor {
    private final Module module;

    public ModuleConvertVisitor(Module module, ModuleVisitor mv) {
        super(Opcodes.ASM6, mv);
        this.module = module;
    }

    @Override
    public void visitMainClass(String mainClass) {
        super.visitMainClass(mainClass);

        module.setMainClass(Optional.of(AsmUtil.pathFromInternalName(mainClass)));
    }

    @Override
    public void visitPackage(String packaze) {
        super.visitPackage(packaze);

        module.getPackages().add(AsmUtil.pathFromInternalName(packaze));
    }

    @Override
    public void visitRequire(String module, int access, String version) {
        super.visitRequire(module, access, version);

        Path modulePath = AsmUtil.pathFromModuleName(module);
        Set<Module.Require.Flag> flags = AccessConverter.moduleRequire.fromBitMap(access);

        this.module.getRequires().add(new Module.Require(modulePath, flags, Optional.ofNullable(version)));
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
        super.visitExport(packaze, access, modules);

        Path packageName = AsmUtil.pathFromInternalName(packaze);
        Set<Module.Export.Flag> flags = AccessConverter.moduleExport.fromBitMap(access);
        List<Path> moduleList = (module == null ? Stream.<String>empty() : Arrays.stream(modules))
                .map(AsmUtil::pathFromModuleName)
                .collect(Collectors.toList());

        this.module.getExports().add(new Module.Export(packageName, flags, moduleList));
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
        super.visitOpen(packaze, access, modules);

        Path packageName = AsmUtil.pathFromInternalName(packaze);
        Set<Module.Open.Flag> flags = AccessConverter.moduleOpen.fromBitMap(access);
        List<Path> moduleList = (module == null ? Stream.<String>empty() : Arrays.stream(modules))
                .map(AsmUtil::pathFromModuleName)
                .collect(Collectors.toList());

        this.module.getOpens().add(new Module.Open(packageName, flags, moduleList));
    }

    @Override
    public void visitUse(String service) {
        super.visitUse(service);

        this.module.getUses().add(AsmUtil.pathFromInternalName(service));
    }

    @Override
    public void visitProvide(String service, String... providers) {
        super.visitProvide(service, providers);

        Path servicePath = AsmUtil.pathFromInternalName(service);
        List<Path> providerPaths = Arrays.stream(providers)
                .map(AsmUtil::pathFromInternalName)
                .collect(Collectors.toList());

        this.module.getProvides().add(new Module.Provide(servicePath, providerPaths));
    }
}
