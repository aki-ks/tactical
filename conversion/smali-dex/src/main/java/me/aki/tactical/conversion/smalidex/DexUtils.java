package me.aki.tactical.conversion.smalidex;

import me.aki.tactical.core.*;
import me.aki.tactical.core.type.*;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodProtoReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DexUtils {
    /**
     * Parse a descriptor that is a object type and get its path.
     *
     * @param descriptor a descriptor of an object type
     * @return path of the object type
     */
    public static Path parseObjectDescriptor(String descriptor) {
        return ((ObjectType) parseDescriptor(descriptor)).getName();
    }

    public static String toObjectDescriptor(Path path) {
        StringBuilder builder = new StringBuilder();
        builder.append('L');
        for (String pkg : path.getPackage()) {
            builder.append(pkg);
            builder.append('/');
        }
        builder.append(path.getName());
        builder.append(';');
        return builder.toString();
    }

    /**
     * Parse a descriptor that might also be a void type.
     *
     * @param descriptor a (return) type descriptor
     * @return the parsed {@link Type}
     */
    public static Optional<Type> parseReturnType(CharSequence descriptor) {
        return parseReturnType(descriptor, 0);
    }

    private static Optional<Type> parseReturnType(CharSequence descriptor, int offset) {
        return descriptor.charAt(offset) == 'V' ? Optional.empty() : Optional.of(parseDescriptor(descriptor, offset));
    }

    /**
     * Convert a jvm type descriptor into a {@link Type}.
     *
     * @param descriptor a type descriptor
     * @return the parsed {@link Type}
     */
    public static Type parseDescriptor(CharSequence descriptor) {
        return parseDescriptor(descriptor, 0);
    }

    private static Type parseDescriptor(CharSequence string, int offset) {
        switch (string.charAt(offset)) {
            case 'Z': return BooleanType.getInstance();
            case 'B': return ByteType.getInstance();
            case 'S': return ShortType.getInstance();
            case 'C': return CharType.getInstance();
            case 'I': return IntType.getInstance();
            case 'J': return LongType.getInstance();
            case 'F': return FloatType.getInstance();
            case 'D': return DoubleType.getInstance();

            case 'L': {
                List<String> pkg = new ArrayList<>();
                StringBuilder builder = new StringBuilder();

                while (true) {
                    char c = string.charAt(++offset);
                    switch (c) {
                        case '/':
                            pkg.add(builder.toString());
                            builder.setLength(0);
                            break;

                        case ';':
                            return new ObjectType(new Path(pkg, builder.toString()));

                        default:
                            builder.append(c);
                    }
                }
            }

            case '[': {
                int dimensions = 1;
                while ((string.charAt(++offset)) == '[') {
                    dimensions += 1;
                }
                return new ArrayType(parseDescriptor(string, offset), dimensions);
            }

            default:
                throw new IllegalArgumentException("Illegal descriptor: " + string.subSequence(offset, string.length()));
        }
    }

    public static MethodDescriptor convertMethodDescriptor(MethodProtoReference methodProto) {
        Optional<Type> returnType = parseReturnType(methodProto.getReturnType());
        List<Type> parameters = methodProto.getParameterTypes().stream()
                .map(DexUtils::parseDescriptor)
                .collect(Collectors.toList());

        return new MethodDescriptor(parameters, returnType);
    }

    /**
     * Convert the {@link Method#getReturnType()} return type} of a {@link Method} to a dex type descriptor.
     *
     * @param type a {@link Method#getReturnType()} return type} of a {@link Method}
     * @return the corresponding dex type descriptor
     */
    public static String toDexReturnType(Optional<Type> type) {
        return type.map(DexUtils::toDexType).orElse("V");
    }

    /**
     * Convert a tactical {@link Type} into a dex type descriptor.
     *
     * @param type a tactical {@link Type}
     * @return the corresponding dex type descriptor
     */
    public static String toDexType(Type type) {
        StringBuilder builder = new StringBuilder();
        toDexType(type, builder);
        return builder.toString();
    }

    private static void toDexType(Type type, StringBuilder builder) {
        if (type instanceof PrimitiveType) {
            if (type instanceof IntLikeType) {
                if (type instanceof BooleanType) {
                    builder.append('Z');
                } else if (type instanceof ByteType) {
                    builder.append('B');
                } else if (type instanceof ShortType) {
                    builder.append('S');
                } else if (type instanceof CharType) {
                    builder.append('C');
                } else if (type instanceof IntType) {
                    builder.append('I');
                } else {
                    throw new AssertionError();
                }
            } else if (type instanceof LongType) {
                builder.append('J');
            } else if (type instanceof FloatType) {
                builder.append('F');
            } else if (type instanceof DoubleType) {
                builder.append('D');
            } else {
                throw new AssertionError();
            }
        } else if (type instanceof RefType) {
            if (type instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) type;
                for (int i = 0; i < arrayType.getDimensions(); i++) {
                    builder.append('[');
                }

                toDexType(arrayType.getBaseType(), builder);
            } else if (type instanceof ObjectType) {
                builder.append('L');

                Path name = ((ObjectType) type).getName();
                for (String pkg : name.getPackage()) {
                    builder.append(pkg);
                    builder.append('/');
                }
                builder.append(name.getName());

                builder.append(';');
            } else {
                throw new AssertionError();
            }
        } else {
            throw new AssertionError();
        }
    }

    public static FieldReference convertFieldRef(FieldRef fieldRef) {
        String definingClass = DexUtils.toObjectDescriptor(fieldRef.getOwner());
        String type = DexUtils.toDexType(fieldRef.getType());
        return new ImmutableFieldReference(definingClass, fieldRef.getName(), type);
    }

    public static MethodReference convertMethodRef(MethodRef methodRef) {
        String definingClass = DexUtils.toObjectDescriptor(methodRef.getOwner());
        String name = methodRef.getName();
        String returnType = DexUtils.toDexReturnType(methodRef.getReturnType());
        List<String> parameters = methodRef.getArguments().stream()
                .map(DexUtils::toDexType)
                .collect(Collectors.toList());

        return new ImmutableMethodReference(definingClass, name, parameters, returnType);
    }

    public static <T> T unreachable() {
        throw new RuntimeException("Unreachable");
    }
}
