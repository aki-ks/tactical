package me.aki.tactical.conversion.stack2asm;

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
import me.aki.tactical.core.typeannotation.TypePath;

import java.util.List;
import java.util.Optional;

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
            return org.objectweb.asm.Type.getObjectType(name.join('/'));
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

    public static String toDescriptor(Type type) {
        return toAsmType(type).getDescriptor();
    }

    public static String methodDescriptorToString(MethodDescriptor descriptor) {
        return methodDescriptorToType(descriptor).getDescriptor();
    }

    public static String methodDescriptorToString(Optional<Type> returnType, List<Type> parameterTypes) {
        return methodDescriptorToType(returnType, parameterTypes).getDescriptor();
    }

    public static org.objectweb.asm.Type methodDescriptorToType(MethodDescriptor descriptor) {
        return methodDescriptorToType(descriptor.getReturnType(), descriptor.getParameterTypes());
    }

    public static org.objectweb.asm.Type methodDescriptorToType(Optional<Type> returnType, List<Type> parameterTypes) {
        org.objectweb.asm.Type asmReturnType = returnType.map(AsmUtil::toAsmType)
                .orElse(org.objectweb.asm.Type.VOID_TYPE);

        org.objectweb.asm.Type[] asmParameterTypes = parameterTypes.stream()
                .map(AsmUtil::toAsmType)
                .toArray(org.objectweb.asm.Type[]::new);

        return org.objectweb.asm.Type.getMethodType(asmReturnType, asmParameterTypes);
    }

    public static String pathToDescriptor(Path path) {
        return "L" + path.join('/') + ";";
    }

    public static org.objectweb.asm.TypePath toAsmTypePath(TypePath typePath) {
        StringBuilder builder = new StringBuilder();
        for (TypePath.Kind kind : typePath.getPaths()) {
            if (kind instanceof TypePath.Kind.Array) {
                builder.append('[');
            } else if (kind instanceof TypePath.Kind.InnerClass) {
                builder.append('.');
            } else if (kind instanceof TypePath.Kind.TypeArgument) {
                int index = ((TypePath.Kind.TypeArgument) kind).getTypeArgumentIndex();
                builder.append(index);
                builder.append(';');
            } else if (kind instanceof TypePath.Kind.WildcardBound) {
                builder.append('*');
            } else {
                throw new AssertionError();
            }
        }
        return org.objectweb.asm.TypePath.fromString(builder.toString());
    }
}
