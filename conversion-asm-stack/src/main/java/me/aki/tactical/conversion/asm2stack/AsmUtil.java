package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.BooleanType;
import me.aki.tactical.core.type.ByteType;
import me.aki.tactical.core.type.CharType;
import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.ShortType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.TypePath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AsmUtil {
    public static Optional<Type> fromAsmReturnType(org.objectweb.asm.Type type) {
        return type.getSort() == org.objectweb.asm.Type.VOID ?
                Optional.empty() : Optional.of(fromAsmType(type));
    }

    /**
     * Convert a jvm descriptor to {@link Type}.
     *
     * @param descriptor to be parsed
     * @return descriptor represented as {@link Type}
     */
    public static Type fromDescriptor(String descriptor) {
        return fromAsmType(org.objectweb.asm.Type.getType(descriptor));
    }

    public static Type fromAsmType(org.objectweb.asm.Type type) {
        switch (type.getSort()) {
            case org.objectweb.asm.Type.VOID: throw new IllegalStateException();

            case org.objectweb.asm.Type.BOOLEAN: return BooleanType.getInstance();
            case org.objectweb.asm.Type.CHAR: return CharType.getInstance();
            case org.objectweb.asm.Type.BYTE: return ByteType.getInstance();
            case org.objectweb.asm.Type.SHORT: return ShortType.getInstance();
            case org.objectweb.asm.Type.INT: return IntType.getInstance();
            case org.objectweb.asm.Type.FLOAT: return FloatType.getInstance();
            case org.objectweb.asm.Type.LONG: return LongType.getInstance();
            case org.objectweb.asm.Type.DOUBLE: return DoubleType.getInstance();

            case org.objectweb.asm.Type.ARRAY:
                Type baseType = fromAsmType(type.getElementType());
                return new ArrayType(baseType, type.getDimensions());

            case org.objectweb.asm.Type.OBJECT:
                Path path = pathFromInternalName(type.getInternalName());
                return new ObjectType(path);

            default: throw new AssertionError();
        }
    }

    /**
     * Get the path of a class file descriptor.
     *
     * @param descriptor descriptor of an object type
     * @return path of the class in the descriptor
     */
    public static Path pathFromObjectDescriptor(String descriptor) {
        String internalName = org.objectweb.asm.Type.getType(descriptor).getInternalName();
        return pathFromInternalName(internalName);
    }

    /**
     * Parse a slash separated class path (e.g. "java/lang/String");
     *
     * @param internalName slash separated class name
     * @return parsed class name
     */
    public static Path pathFromInternalName(String internalName) {
        return pathFromSeperatedString(internalName, '/');
    }

    /**
     * Parse a dot separated module path.
     *
     * @param moduleName dot separated module path
     * @return parsed module path
     */
    public static Path pathFromModuleName(String moduleName) {
        return pathFromSeperatedString(moduleName, '.');
    }

    private static Path pathFromSeperatedString(String string, char seperator) {
        List<String> pkgs = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (character == seperator) {
                pkgs.add(builder.toString());
                builder.setLength(0);
            } else {
                builder.append(character);
            }
        }

        return new Path(pkgs, builder.toString());
    }

    public static MethodDescriptor parseMethodDescriptor(String descriptor) {
        return parseMethodDescriptor(org.objectweb.asm.Type.getType(descriptor));
    }

    public static MethodDescriptor parseMethodDescriptor(org.objectweb.asm.Type methodType) {
        List<Type> convertedArgTypes = Arrays.stream(methodType.getArgumentTypes())
                .map(AsmUtil::fromAsmType)
                .collect(Collectors.toList());

        return new MethodDescriptor(convertedArgTypes, fromAsmReturnType(methodType.getReturnType()));
    }

    /**
     * Convert an asm TypePath representation to a our {@link TypePath} representation.
     *
     * @param asmPath type path to be converted
     * @return converted type path
     */
    public static TypePath fromAsmTypePath(org.objectweb.asm.TypePath asmPath) {
        if (asmPath == null) {
            return new TypePath(new ArrayList<>());
        } else {
            int length = asmPath.getLength();
            List<TypePath.Kind> kinds = new ArrayList<>(length);
            for (int step = 0; step < length; step++) {
                kinds.add(convertTypePathKind(asmPath, step));
            }
            return new TypePath(kinds);
        }
    }

    private static TypePath.Kind convertTypePathKind(org.objectweb.asm.TypePath asmPath, int step) {
        switch (asmPath.getStep(step)) {
            case org.objectweb.asm.TypePath.ARRAY_ELEMENT:
                return new TypePath.Kind.Array();

            case org.objectweb.asm.TypePath.INNER_TYPE:
                return new TypePath.Kind.InnerClass();

            case org.objectweb.asm.TypePath.WILDCARD_BOUND:
                return new TypePath.Kind.WildcardBound();

            case org.objectweb.asm.TypePath.TYPE_ARGUMENT:
                return new TypePath.Kind.TypeArgument(asmPath.getStepArgument(step));

            default:
                throw new AssertionError();
        }
    }

    public static RefType refTypeFromInternalName(String internalName) {
        return (RefType) fromAsmType(org.objectweb.asm.Type.getObjectType(internalName));
    }
}
