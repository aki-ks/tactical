package me.aki.tactical.core.textify;

import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Module;

import java.util.Map;
import java.util.Set;

public class FlagTextifier<F extends Enum<F>> implements Textifier<Set<F>> {
    public static final FlagTextifier<Classfile.Flag> CLASS = new FlagTextifier<>(Map.of(
            Classfile.Flag.PUBLIC, "public",
            Classfile.Flag.FINAL,  "final",
            Classfile.Flag.SUPER, "super",
            Classfile.Flag.ABSTRACT, "abstract",
            Classfile.Flag.SYNTHETIC, "synthetic"
//            Classfile.Flag.INTERFACE, "interface",
//            Classfile.Flag.ANNOTATION, "@interface",
//            Classfile.Flag.ENUM, "enum",
//            Classfile.Flag.MODULE, "module"
    ));

    public static final FlagTextifier<Classfile.InnerClass.Flag> INNER_CLASS = new FlagTextifier<>(Map.of(
            Classfile.InnerClass.Flag.PUBLIC, "public",
            Classfile.InnerClass.Flag.PRIVATE, "private",
            Classfile.InnerClass.Flag.PROTECTED,  "protected",
            Classfile.InnerClass.Flag.STATIC, "static",
            Classfile.InnerClass.Flag.FINAL, "final",
            Classfile.InnerClass.Flag.SYNTHETIC, "sythetic",
            Classfile.InnerClass.Flag.ABSTRACT, "abstract",

            Classfile.InnerClass.Flag.INTERFACE, "interface",
            Classfile.InnerClass.Flag.ANNOTATION, "@interface",
            Classfile.InnerClass.Flag.ENUM, "enum"
    ));

    public static final FlagTextifier<Module.Flag> MODULE = new FlagTextifier<>(Map.of(
            Module.Flag.OPEN, "open",
            Module.Flag.SYNTHETIC, "synthetic",
            Module.Flag.MANDATED, "mandated"
    ));

    public static final FlagTextifier<Module.Require.Flag> MODULE_REQUIRES = new FlagTextifier<>(Map.of(
            Module.Require.Flag.TRANSITIVE, "transitive",
            Module.Require.Flag.STATIC_PHASE, "static-phase",
            Module.Require.Flag.SYNTHETIC, "synthetic",
            Module.Require.Flag.MANDATED, "mandated"
    ));

    public static final FlagTextifier<Module.Export.Flag> MODULE_EXPORTS = new FlagTextifier<>(Map.of(
            Module.Export.Flag.SYNTHETIC, "synthetic",
            Module.Export.Flag.MANDATED, "mandated"
    ));

    public static final FlagTextifier<Module.Open.Flag> MODULE_OPENS = new FlagTextifier<>(Map.of(
            Module.Open.Flag.SYNTHETIC, "synthetic",
            Module.Open.Flag.MANDATED, "mandated"
    ));

    private Map<F, String> nameMap;

    public FlagTextifier(Map<F, String> nameMap) {
        this.nameMap = nameMap;
    }

    @Override
    public void textify(Printer printer, Set<F> flags) {
        nameMap.forEach((flag, string) -> {
            if (flags.contains(flag)) {
                printer.addText(string + " ");
            }
        });
    }
}
