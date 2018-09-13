package me.aki.tactical.core.typeannotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TypePath {
    /**
     * Describes what part of the type is annotated.
     *
     * Empty if the whole type is annotated.
     */
    private List<TypePath.Kind> paths;

    public TypePath(List<TypePath.Kind> paths) {
        this.paths = paths;
    }

    public List<TypePath.Kind> getPaths() {
        return paths;
    }

    public void setPaths(List<TypePath.Kind> paths) {
        this.paths = paths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypePath typePath = (TypePath) o;
        return Objects.equals(paths, typePath.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paths);
    }

    @Override
    public String toString() {
        return TypePath.class.getSimpleName() + '{' +
                "paths=" + paths +
                '}';
    }

    public interface Kind {
        /**
         * Constant that can be used with switch statements.
         */
        int getSort();

        /**
         * Annotate the basetype of an array.
         *
         * Example:
         * <pre><code>
         *     @MyTypeAnnotation String[]
         * </code></pre>
         */
        final class Array implements Kind {
            private static final int SORT = 0;

            @Override
            public int getSort() {
                return SORT;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof Array;
            }

            @Override
            public int hashCode() {
                return getSort();
            }

            @Override
            public String toString() {
                return Array.class.getSimpleName() + "{}";
            }
        }

        /**
         * Annotate a (non-static) inner class.
         *
         * Examples:
         * <pre><code>
         *     Outer.@MyAnnotation Inner
         * <pre><code>
         */
        final class InnerClass implements Kind {
            public static final int SORT = 1;

            @Override
            public int getSort() {
                return SORT;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof InnerClass;
            }

            @Override
            public int hashCode() {
                return getSort();
            }

            @Override
            public String toString() {
                return InnerClass.class.getSimpleName() + "{}";
            }
        }

        /**
         * Annotate the bound of a wildcard.
         *
         * Examples:
         * <pre><code>
         *     ? extends @MyAnnotation T
         * </code></pre>
         * <pre><code>
         *     ? super @MyAnnotation String
         * </code></pre>
         */
        final class WildcardBound implements Kind {
            public static final int SORT = 2;

            @Override
            public int getSort() {
                return SORT;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof WildcardBound;
            }

            @Override
            public int hashCode() {
                return getSort();
            }

            @Override
            public String toString() {
                return WildcardBound.class.getSimpleName() + "{}";
            }
        }

        /**
         * Annotate a TypeArgument of a Type.
         *
         * Examples:
         * <pre><code>Map<@MyAnnotation String, Long></code></pre>
         * <pre><code>new TypeArgument(0)</code></pre>
         *
         * <pre><code>Map<String, @MyAnnotation Long></code></pre>
         * <pre><code>new TypeArgument(1)</code></pre>
         */
        final class TypeArgument implements Kind {
            public static final int SORT = 3;

            /**
             * Index of the annotated type argument.
             */
            private int index;

            public TypeArgument(int index) {
                this.index = index;
            }

            public int getTypeArgumentIndex() {
                return index;
            }

            public void setTypeArgumentIndex(int index) {
                this.index = index;
            }

            @Override
            public int getSort() {
                return SORT;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                TypeArgument that = (TypeArgument) o;
                return index == that.index;
            }

            @Override
            public int hashCode() {
                return Objects.hash(getSort(), index);
            }

            @Override
            public String toString() {
                return TypeArgument.class.getSimpleName() + "{}";
            }
        }
    }
}
