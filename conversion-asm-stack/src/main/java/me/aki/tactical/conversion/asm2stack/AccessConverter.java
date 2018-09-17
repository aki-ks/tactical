package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Module;
import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convert a bitmap as used by asm to a set of flags.
 *
 * @param <F> flag enum
 */
public class AccessConverter<F> {
    public static final AccessConverter<Classfile.Flag> classfile = new AccessConverter<>(Map.of(
            Classfile.Flag.PUBLIC, Opcodes.ACC_PUBLIC,
            Classfile.Flag.FINAL, Opcodes.ACC_FINAL,
            Classfile.Flag.SUPER, Opcodes.ACC_SUPER,
            Classfile.Flag.INTERFACE, Opcodes.ACC_INTERFACE,
            Classfile.Flag.ABSTRACT, Opcodes.ACC_ABSTRACT,
            Classfile.Flag.SYNTHETIC, Opcodes.ACC_SYNTHETIC,
            Classfile.Flag.ANNOTATION, Opcodes.ACC_ANNOTATION,
            Classfile.Flag.ENUM, Opcodes.ACC_ENUM,
            Classfile.Flag.MODULE, Opcodes.ACC_MODULE
    ));

    public static final AccessConverter<Module.Flag> module = new AccessConverter<>(Map.of(
            Module.Flag.OPEN, Opcodes.ACC_OPEN,
            Module.Flag.SYNTHETIC, Opcodes.ACC_SYNTHETIC,
            Module.Flag.MANDATED, Opcodes.ACC_MANDATED
    ));

    public static final AccessConverter<Module.Require.Flag> moduleRequire = new AccessConverter<>(Map.of(
            Module.Require.Flag.TRANSITIVE, Opcodes.ACC_TRANSITIVE,
            Module.Require.Flag.STATIC_PHASE, Opcodes.ACC_STATIC_PHASE,
            Module.Require.Flag.SYNTHETIC, Opcodes.ACC_SYNTHETIC,
            Module.Require.Flag.MANDATED, Opcodes.ACC_MANDATED
    ));

    public static final AccessConverter<Module.Export.Flag> moduleExport = new AccessConverter<>(Map.of(
            Module.Export.Flag.MANDATED, Opcodes.ACC_MANDATED,
            Module.Export.Flag.SYNTHETIC, Opcodes.ACC_SYNTHETIC
    ));

    public static final AccessConverter<Module.Open.Flag> moduleOpen = new AccessConverter<>(Map.of(
            Module.Open.Flag.MANDATED, Opcodes.ACC_MANDATED,
            Module.Open.Flag.SYNTHETIC, Opcodes.ACC_SYNTHETIC
    ));

    private final Map<F, Integer> flagMap;

    public AccessConverter(Map<F, Integer> flagMap) {
        this.flagMap = flagMap;
    }

    public Set<F> fromBitMap(int bitmap) {
        return flagMap.entrySet().stream()
                .filter(e -> (bitmap & e.getValue()) != 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public int toBitMap(Set<F> flags) {
        return flags.stream()
                .mapToInt(flagMap::get)
                .reduce(0, (a, b) -> a | b);
    }
}
