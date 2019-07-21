package me.aki.tactical.conversion.smalidex;

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
import me.aki.tactical.core.type.ShortType;
import me.aki.tactical.core.type.Type;
import org.jf.dexlib2.iface.reference.MethodProtoReference;

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
}
