package me.aki.tactical.conversion.smalidex;

import me.aki.tactical.core.Classfile;
import me.aki.tactical.core.Field;
import me.aki.tactical.core.Method;
import org.jf.dexlib2.AccessFlags;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convert a bitmap as used by smali to a set of flags.
 *
 * @param <F> flag enum
 */
public class FlagConverter<F extends Enum<F>> {
    public static final FlagConverter<Classfile.Flag> CLASSFILE = new FlagConverter<>(Map.of(
            Classfile.Flag.PUBLIC, AccessFlags.PUBLIC,
            Classfile.Flag.FINAL, AccessFlags.FINAL,
            Classfile.Flag.INTERFACE, AccessFlags.INTERFACE,
            Classfile.Flag.ABSTRACT, AccessFlags.ABSTRACT,
            Classfile.Flag.SYNTHETIC, AccessFlags.SYNTHETIC,
            Classfile.Flag.ANNOTATION, AccessFlags.ANNOTATION,
            Classfile.Flag.ENUM, AccessFlags.ENUM
    ));

    public static final FlagConverter<Classfile.InnerClass.Flag> INNER_CLASS = new FlagConverter<>(Map.of(
            Classfile.InnerClass.Flag.PUBLIC, AccessFlags.PUBLIC,
            Classfile.InnerClass.Flag.PRIVATE, AccessFlags.PRIVATE,
            Classfile.InnerClass.Flag.PROTECTED, AccessFlags.PROTECTED,
            Classfile.InnerClass.Flag.STATIC, AccessFlags.STATIC,
            Classfile.InnerClass.Flag.FINAL, AccessFlags.FINAL,
            Classfile.InnerClass.Flag.INTERFACE, AccessFlags.INTERFACE,
            Classfile.InnerClass.Flag.ABSTRACT, AccessFlags.ABSTRACT,
            Classfile.InnerClass.Flag.SYNTHETIC, AccessFlags.SYNTHETIC,
            Classfile.InnerClass.Flag.ANNOTATION, AccessFlags.ANNOTATION,
            Classfile.InnerClass.Flag.ENUM, AccessFlags.ENUM
    ));

    public static final FlagConverter<Field.Flag> FIELD = new FlagConverter<>(Map.of(
            Field.Flag.PUBLIC, AccessFlags.PUBLIC,
            Field.Flag.PRIVATE, AccessFlags.PRIVATE,
            Field.Flag.PROTECTED, AccessFlags.PROTECTED,
            Field.Flag.STATIC, AccessFlags.STATIC,
            Field.Flag.FINAL, AccessFlags.FINAL,
            Field.Flag.VOLATILE, AccessFlags.VOLATILE,
            Field.Flag.TRANSIENT, AccessFlags.TRANSIENT,
            Field.Flag.SYNTHETIC, AccessFlags.SYNTHETIC,
            Field.Flag.ENUM, AccessFlags.ENUM
    ));

    public static final FlagConverter<Method.Flag> METHOD = new FlagConverter<>(Map.ofEntries(
            Map.entry(Method.Flag.PUBLIC, AccessFlags.PUBLIC),
            Map.entry(Method.Flag.PRIVATE, AccessFlags.PRIVATE),
            Map.entry(Method.Flag.PROTECTED, AccessFlags.PROTECTED),
            Map.entry(Method.Flag.STATIC, AccessFlags.STATIC),
            Map.entry(Method.Flag.FINAL, AccessFlags.FINAL),
            Map.entry(Method.Flag.SYNCHRONIZED, AccessFlags.SYNCHRONIZED),
            Map.entry(Method.Flag.BRIDGE, AccessFlags.BRIDGE),
            Map.entry(Method.Flag.VARARGS, AccessFlags.VARARGS),
            Map.entry(Method.Flag.NATIVE, AccessFlags.NATIVE),
            Map.entry(Method.Flag.ABSTRACT, AccessFlags.ABSTRACT),
            Map.entry(Method.Flag.STRICT, AccessFlags.STRICTFP),
            Map.entry(Method.Flag.SYNTHETIC, AccessFlags.SYNTHETIC)
    ));

    private final Map<F, Integer> flagMap;

    public FlagConverter(Map<F, AccessFlags> flagMap) {
        this.flagMap = flagMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue()));
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
