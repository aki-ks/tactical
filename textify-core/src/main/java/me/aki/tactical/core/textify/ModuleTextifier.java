package me.aki.tactical.core.textify;

import me.aki.tactical.core.Module;
import me.aki.tactical.core.Path;

import java.util.List;

public class ModuleTextifier implements Textifier<Module> {
    private static final ModuleTextifier INSTANCE = new ModuleTextifier();

    public static ModuleTextifier getInstance() {
        return INSTANCE;
    }

    public static final Textifier<String> VERSION = (printer, version) -> {
        printer.addText("version ");
        printer.addEscaped(version, '"');
        printer.addText(";");
    };

    public static final Textifier<Path> MAIN_CLASS = (printer, main) -> {
        printer.addText("main ");
        printer.addPath(main);
        printer.addText(";");
    };

    public static final Textifier<Path> PACKAGE = (printer, pkg) -> {
        printer.addText("package ");
        printer.addPath(pkg);
        printer.addText(";");
    };

    public static final Textifier<Module.Require> REQUIRE = (printer, requires) -> {
        FlagTextifier.MODULE_REQUIRES.textify(printer, requires.getFlags());
        printer.addText("requires ");
        printer.addPath(requires.getName());
        requires.getVersion().ifPresent(version -> {
            printer.addText(" : ");
            printer.addEscaped(version, '"');
        });
        printer.addText(";");
    };

    public static final Textifier<Module.Export> EXPORT = (printer, exports) -> {
        FlagTextifier.MODULE_EXPORTS.textify(printer, exports.getFlags());
        printer.addText("exports ");
        printer.addPath(exports.getName());

        List<Path> modules = exports.getModules();
        if (!modules.isEmpty()) {
            printer.addText(" to ");
            TextUtil.joined(modules, printer::addPath, () -> printer.addText(", "));
        }

        printer.addText(";");
    };

    public static final Textifier<Module.Open> OPEN = (printer, opens) -> {
        FlagTextifier.MODULE_OPENS.textify(printer, opens.getFlags());
        printer.addText("opens ");
        printer.addPath(opens.getName());

        List<Path> modules = opens.getModules();
        if (!modules.isEmpty()) {
            printer.addText(" to ");
            TextUtil.joined(modules, printer::addPath, () -> printer.addText(", "));
        }

        printer.addText(";");
    };

    public static final Textifier<Path> USE = (printer, use) -> {
        printer.addText("uses ");
        printer.addPath(use);
        printer.addText(";");
    };

    public static final Textifier<Module.Provide> PROVIDES = (printer, provide) -> {
        printer.addText("provides ");
        printer.addPath(provide.getService());

        List<Path> providers = provide.getProviders();
        if (!providers.isEmpty()) {
            printer.addText(" with ");
            providers.forEach(printer::addPath);
        }

        printer.addText(";");
    };

    @Override
    public void textify(Printer printer, Module module) {
        FlagTextifier.MODULE.textify(printer, module.getAccessFlags());
        printer.addText("module ");
        printer.addPath(module.getModule());
        printer.addText(" {");
        printer.newLine();
        printer.increaseIndent();

        module.getVersion().ifPresent(version -> {
            VERSION.textify(printer, version);
            printer.newLine();
        });

        module.getMainClass().ifPresent(main -> {
            MAIN_CLASS.textify(printer, main);
            printer.newLine();
        });

        for (Path pkg : module.getPackages()) {
            PACKAGE.textify(printer, pkg);
            printer.newLine();
        }

        for (Module.Require requires : module.getRequires()) {
            REQUIRE.textify(printer, requires);
            printer.newLine();
        }

        for (Module.Export exports : module.getExports()) {
            EXPORT.textify(printer, exports);
            printer.newLine();
        }

        for (Module.Open opens : module.getOpens()) {
            OPEN.textify(printer, opens);
            printer.newLine();
        }

        for (Path use : module.getUses()) {
            USE.textify(printer, use);
            printer.newLine();
        }

        for (Module.Provide provide : module.getProvides()) {
            PROVIDES.textify(printer, provide);
            printer.newLine();
        }

        printer.decreaseIndent();
        printer.addText("}");
    }
}
