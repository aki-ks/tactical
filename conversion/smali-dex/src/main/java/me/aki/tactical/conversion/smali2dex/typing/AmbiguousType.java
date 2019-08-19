package me.aki.tactical.conversion.smali2dex.typing;

import me.aki.tactical.core.type.Type;

/**
 * A type that is not exactly known.
 */
public interface AmbiguousType extends Type {
    public static class IntOrFloatOrRef implements AmbiguousType {
        private static final IntOrFloatOrRef INSTANCE = new IntOrFloatOrRef();

        public static IntOrFloatOrRef getInstance() {
            return INSTANCE;
        }

        private IntOrFloatOrRef() {}

        @Override
        public String toString() {
            return IntOrFloatOrRef.class.getSimpleName() + "{}";
        }
    }

    public static class IntOrFloat implements AmbiguousType {
        private static final IntOrFloat INSTANCE = new IntOrFloat();

        public static IntOrFloat getInstance() {
            return INSTANCE;
        }

        private IntOrFloat() {}

        public String toString() {
            return IntOrFloat.class.getSimpleName() + "{}";
        }
    }

    public static class LongOrDouble implements AmbiguousType {
        private static final LongOrDouble INSTANCE = new LongOrDouble();

        public static LongOrDouble getInstance() {
            return INSTANCE;
        }

        private LongOrDouble() {}

        @Override
        public String toString() {
            return LongOrDouble.class.getSimpleName() + "{}";
        }
    }
}
