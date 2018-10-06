package me.aki.tactical.core.textify;

import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Field;
import me.aki.tactical.core.Method;
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

    public static final FlagTextifier<Field.Flag> FIELD = new FlagTextifier<>(Map.of(
            Field.Flag.PUBLIC, "public",
            Field.Flag.PRIVATE, "private",
            Field.Flag.PROTECTED, "protected",
            Field.Flag.STATIC, "static",
            Field.Flag.FINAL, "final",
            Field.Flag.VOLATILE, "volatile",
            Field.Flag.TRANSIENT, "transient",
            Field.Flag.SYNTHETIC, "synthetic",
            Field.Flag.ENUM, "enum"
    ));

    public static final FlagTextifier<Method.Flag> METHOD = new FlagTextifier<>(Map.ofEntries(
            Map.entry(Method.Flag.PUBLIC, "public"),
            Map.entry(Method.Flag.PRIVATE, "private"),
            Map.entry(Method.Flag.PROTECTED, "protected"),
            Map.entry(Method.Flag.STATIC, "static"),
            Map.entry(Method.Flag.FINAL, "final"),
            Map.entry(Method.Flag.SYNCHRONIZED, "synchronized"),
            Map.entry(Method.Flag.BRIDGE, "bridge"),
            Map.entry(Method.Flag.VARARGS, "varargs"),
            Map.entry(Method.Flag.NATIVE, "native"),
            Map.entry(Method.Flag.ABSTRACT, "abstract"),
            Map.entry(Method.Flag.STRICT, "strict"),
            Map.entry(Method.Flag.SYNTHETIC, "synthetic")
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
