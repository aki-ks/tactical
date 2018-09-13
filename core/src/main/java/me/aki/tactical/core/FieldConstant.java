package me.aki.tactical.core;

import java.util.Objects;

/**
 * A the value of constant (a "static final" field of primitive or String type).
 *
 * @see Field#constant
 */
public interface FieldConstant {
    public static class DoubleConstant implements FieldConstant {
        private final double value;

        public DoubleConstant(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DoubleConstant that = (DoubleConstant) o;
            return Double.compare(that.value, value) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return DoubleConstant.class.getSimpleName() + '{' +
                    "value=" + value +
                    '}';
        }
    }

    public static class FloatConstant implements FieldConstant {
        private final float value;

        public FloatConstant(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FloatConstant that = (FloatConstant) o;
            return Float.compare(that.value, value) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return FloatConstant.class.getSimpleName() + '{' +
                    "value=" + value +
                    '}';
        }
    }

    public static class IntConstant implements FieldConstant {
        private final int value;

        public IntConstant(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntConstant that = (IntConstant) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return IntConstant.class.getSimpleName() + '{' +
                    "value=" + value +
                    '}';
        }
    }

    public static class LongConstant implements FieldConstant {
        private final long value;

        public LongConstant(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LongConstant that = (LongConstant) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return LongConstant.class.getSimpleName() + '{' +
                    "value=" + value +
                    '}';
        }
    }

    /**
     * An instance of "java.lang.String".
     */
    public static class StringConstant implements FieldConstant {
        private final String value;

        public StringConstant(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringConstant that = (StringConstant) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return StringConstant.class.getSimpleName() + '{' +
                    "value='" + value + '\'' +
                    '}';
        }
    }
}
