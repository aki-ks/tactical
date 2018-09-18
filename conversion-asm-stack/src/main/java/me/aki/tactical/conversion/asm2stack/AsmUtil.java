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
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.ShortType;
import me.aki.tactical.core.type.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AsmUtil {
    public static org.objectweb.asm.Type toAsmReturnType(Optional<Type> type) {
        return type.map(AsmUtil::toAsmType).orElse(org.objectweb.asm.Type.VOID_TYPE);
    }

    public static org.objectweb.asm.Type toAsmType(Type type) {
        if (type instanceof PrimitiveType) {
            // most used primitive types
            if (type instanceof IntType) {
                return org.objectweb.asm.Type.INT_TYPE;
            } else if (type instanceof LongType) {
                return org.objectweb.asm.Type.LONG_TYPE;
            } else if (type instanceof FloatType) {
                return org.objectweb.asm.Type.FLOAT_TYPE;
            } else if (type instanceof DoubleType) {
                return org.objectweb.asm.Type.DOUBLE_TYPE;
            }

            // less common types
            if (type instanceof BooleanType) {
                return org.objectweb.asm.Type.BOOLEAN_TYPE;
            } else if (type instanceof ByteType) {
                return org.objectweb.asm.Type.BYTE_TYPE;
            } else if (type instanceof ShortType) {
                return org.objectweb.asm.Type.SHORT_TYPE;
            } else if (type instanceof CharType) {
                return org.objectweb.asm.Type.CHAR_TYPE;
            }
        } else if (type instanceof ObjectType) {
            Path name = ((ObjectType) type).getName();
            return org.objectweb.asm.Type.getObjectType(joinPath(name, '/'));
        } else if (type instanceof ArrayType) {
            ArrayType array = (ArrayType) type;
            int dimensions = array.getDimensions();
            String baseDescriptor = toAsmType(array.getBaseType()).getDescriptor();

            StringBuilder builder = new StringBuilder(dimensions + baseDescriptor.length());

            for (int i = 0; i < dimensions; i++) {
                builder.append('[');
            }

            builder.append(baseDescriptor);

            return org.objectweb.asm.Type.getType(builder.toString());
        }

        throw new AssertionError();
    }

    public static Optional<Type> fromAsmReturnType(org.objectweb.asm.Type type) {
        return type.getSort() == org.objectweb.asm.Type.VOID ?
                Optional.empty() : Optional.of(fromAsmType(type));
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

    /**
     * Concatenate packages and class name.
     *
     * @param path to be joined
     * @param separator between all packages and the classname.
     * @return the joined path.
     */
    public static String joinPath(Path path, char separator) {
        StringBuilder builder = new StringBuilder();

        for (String pkg : path.getPackage()) {
            builder.append(pkg);
            builder.append(separator);
        }

        builder.append(path.getName());
        return builder.toString();
    }

    public static MethodDescriptor parseMethodDescriptor(String descriptor) {
        org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getReturnType(descriptor);
        org.objectweb.asm.Type[] argumentTypes = org.objectweb.asm.Type.getArgumentTypes(descriptor);

        List<Type> convertedArgTypes = Arrays.stream(argumentTypes)
                .map(AsmUtil::fromAsmType)
                .collect(Collectors.toList());

        return new MethodDescriptor(convertedArgTypes, fromAsmReturnType(returnType));
    }
}
