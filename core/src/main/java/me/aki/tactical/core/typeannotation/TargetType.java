package me.aki.tactical.core.typeannotation;

import java.util.Objects;

public interface TargetType {

    /**
     * Constant that allows to switch on the type.
     */
    int getSort();

    interface MethodTargetType extends TargetType {
        int SORT_CHECKED_EXCEPTION = CheckedException.SORT;
        int SORT_PARAMETER = MethodParameter.SORT;
        int SORT_RECEIVER = MethodReceiver.SORT;
        int SORT_RETURN_TYPE = ReturnType.SORT;
        int SORT_TYPE_PARAMETER = TypeParameter.SORT;
        int SORT_TYPE_PARAMETER_BOUND = TypeParameterBound.SORT;
    }

    interface ClassTargetType extends TargetType {
        int SORT_EXTENDS = Extends.SORT;
        int SORT_IMPLEMENTS = Implements.SORT;
        int SORT_TYPE_PARAMETER = TypeParameter.SORT;
        int SORT_TYPE_PARAMETER_BOUND = TypeParameterBound.SORT;
    }

    interface InsnTargetType extends TargetType {
        int SORT_INSTANCE_OF = InstanceOf.SORT;
        int SORT_NEW = New.SORT;
        int SORT_CONSTRUCTOR_REFERENCE = ConstructorReference.SORT;
        int SORT_METHOD_REFERENCE = MethodReference.SORT;
        int SORT_CAST = Cast.SORT;
        int SORT_CONSTRUCTOR_INVOKE_TYPE_PARAMETER = ConstructorInvokeTypeParameter.SORT;
        int SORT_METHOD_INVOKE_TYPE_PARAMETER = MethodInvokeTypeParameter.SORT;
        int SORT_CONSTRUCTOR_REFERENCE_TYPE_PARAMETER = ConstructorReferenceTypeParameter.SORT;
        int SORT_METHOD_REFERENCE_TYPE_PARAMETER = MethodReferenceTypeParameter.SORT;
    }

    /**
     * Annotate a bound of a type parameter declaration.
     *
     * example:
     * <pre><code>
     *     class X<T extends @MyAnno Number>
     * </code></pre>
     * <pre><code>
     *     class X<T extends Number & @MyAnno Cloneable>
     * </code></pre>
     */
    final class TypeParameterBound implements ClassTargetType, MethodTargetType {
        public static final int SORT = 0;

        /**
         * Index of type parameter declaration.
         */
        private int parameterIndex;

        /**
         * Index of the bound to be annotated.
         */
        private int boundIndex;

        public TypeParameterBound(int parameterIndex, int boundIndex) {
            this.parameterIndex = parameterIndex;
            this.boundIndex = boundIndex;
        }

        public int getParameterIndex() {
            return parameterIndex;
        }

        public void setParameterIndex(int parameterIndex) {
            this.parameterIndex = parameterIndex;
        }

        public int getBoundIndex() {
            return boundIndex;
        }

        public void setBoundIndex(int boundIndex) {
            this.boundIndex = boundIndex;
        }

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeParameterBound that = (TypeParameterBound) o;
            return parameterIndex == that.parameterIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), parameterIndex);
        }

        @Override
        public String toString() {
            return TypeParameterBound.class.getSimpleName() + '{' +
                    "parameterIndex=" + parameterIndex +
                    ", boundIndex=" + boundIndex +
                    '}';
        }
    }

    /**
     * Annotate the supertype of a class
     *
     * example:
     * <pre><code>
     *     class X extends @MyAnno Number
     * </code></pre>
     */
    final class Extends implements ClassTargetType {
        public static final int SORT = 1;

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Extends;
        }

        @Override
        public int hashCode() {
            return getSort();
        }

        @Override
        public String toString() {
            return Extends.class.getSimpleName() + "{}";
        }
    }

    /**
     * Annotate interfaces implemented by a class or
     * interface extended by another interface.
     *
     * examples:
     * <pre><code>
     *     class X implementes @MyAnno Cloneable
     * </code></pre>
     * <pre><code>
     *     interface Y extends @MyAnno Cloneable
     * </code></pre>
     */
    final class Implements implements ClassTargetType {
        public static final int SORT = 2;

        /**
         * index of interface to be annotated
         */
        private int index;

        public Implements(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
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
            Implements that = (Implements) o;
            return index == that.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), index);
        }

        @Override
        public String toString() {
            return Implements.class.getSimpleName() + '{' +
                    "index=" + index +
                    '}';
        }
    }

    /**
     * Annotate the declaration of a type parameter.
     *
     * example:
     * <pre><code>
     *     class List<@MyAnno T>
     * </code></pre>
     */
    final class TypeParameter implements ClassTargetType, MethodTargetType {
        public static final int SORT = 3;

        /**
         * Index of type parameter to be annotated.
         */
        private int parameterIndex;

        public TypeParameter(int parameterIndex) {
            this.parameterIndex = parameterIndex;
        }

        public int getParameterIndex() {
            return parameterIndex;
        }

        public void setParameterIndex(int parameterIndex) {
            this.parameterIndex = parameterIndex;
        }

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeParameter that = (TypeParameter) o;
            return parameterIndex == that.parameterIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), parameterIndex);
        }

        @Override
        public String toString() {
            return TypeParameter.class.getSimpleName() + '{' +
                    "parameterIndex=" + parameterIndex +
                    '}';
        }
    }

    /**
     * Annotate the return type of a method.
     *
     * example:
     * <pre><code>
     *     public @MyAnno String foo() { ... }
     * </code></pre>
     */
    final class ReturnType implements MethodTargetType {
        public static final int SORT = 4;

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ReturnType;
        }

        @Override
        public int hashCode() {
            return getSort();
        }

        @Override
        public String toString() {
            return ReturnType.class.getSimpleName() + "{}";
        }
    }

    /**
     * Annotate the receiver ("this" parameter) of a method.
     *
     * example:
     * <pre><code>
     *     public class ExampleClass {
     *         public void foo(@MyAnno ExampleClass this) { ... }
     *     }
     * </code></pre>
     */
    final class MethodReceiver implements MethodTargetType {
        public static final int SORT = 5;

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof MethodReceiver;
        }

        @Override
        public int hashCode() {
            return getSort();
        }

        @Override
        public String toString() {
            return MethodReceiver.class.getSimpleName() + "{}";
        }
    }

    /**
     * Annotate a parameter of a method.
     *
     * example:
     * <pre><code>
     *     public void foo(@MyAnno String s) { ... }
     * </code></pre>
     */
    final class MethodParameter implements MethodTargetType {
        public static final int SORT = 6;

        /**
         * Index of annotated parameter.
         */
        private int parameter;

        public MethodParameter(int parameter) {
            this.parameter = parameter;
        }

        public int getParameter() {
            return parameter;
        }

        public void setParameter(int parameter) {
            this.parameter = parameter;
        }

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodParameter that = (MethodParameter) o;
            return parameter == that.parameter;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), parameter);
        }

        @Override
        public String toString() {
            return MethodParameter.class.getSimpleName() + '{' +
                    "parameter=" + parameter +
                    '}';
        }
    }

    /**
     * Annotate a checked exception within a method declaration.
     *
     * example:
     * <pre><code>
     *     public void foo() throws @MyAnno IOException { ... }
     * </code></pre>
     */
    final class CheckedException implements MethodTargetType {
        public static final int SORT = 7;

        /**
         * Index of annotated checked exception.
         */
        private int exception;

        public CheckedException(int exception) {
            this.exception = exception;
        }

        public int getException() {
            return exception;
        }

        public void setException(int exception) {
            this.exception = exception;
        }

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CheckedException that = (CheckedException) o;
            return exception == that.exception;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), exception);
        }

        @Override
        public String toString() {
            return CheckedException.class.getSimpleName() + '{' +
                    "exception=" + exception +
                    '}';
        }
    }

    /**
     * Annotate the type in a "instanceof" statement.
     *
     * Example:
     * <pre><code>obj instanceOf @MyAnno String</code></pre>
     */
    final class InstanceOf implements InsnTargetType {
        public static final int SORT = 8;

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof InstanceOf;
        }

        @Override
        public int hashCode() {
            return getSort();
        }

        @Override
        public String toString() {
            return InstanceOf.class.getSimpleName() + "{}";
        }
    }

    /**
     * Annotate the class in a "new" expression.
     *
     * Example:
     * <pre><code>new @MyAnno Object()</code></pre>
     */
    final class New implements InsnTargetType {
        public static final int SORT = 9;

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof New;
        }

        @Override
        public int hashCode() {
            return getSort();
        }

        @Override
        public String toString() {
            return New.class.getSimpleName() + "{}";
        }
    }

    /**
     * Annotate the class in a "::new" expression.
     *
     * Example:
     * <pre><code>@MyAnno Object::new</code></pre>
     */
    final class ConstructorReference implements InsnTargetType {
        public static final int SORT = 10;

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ConstructorReference;
        }

        @Override
        public int hashCode() {
            return getSort();
        }

        @Override
        public String toString() {
            return ConstructorReference.class.getSimpleName() + "{}";
        }
    }

    /**
     * Annotate the class in a method reference
     *
     * Example:
     * <pre><code>@MyAnno Object::toString</code></pre>
     */
    final class MethodReference implements InsnTargetType {
        public static final int SORT = 11;

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof MethodReference;
        }

        @Override
        public int hashCode() {
            return getSort();
        }

        @Override
        public String toString() {
            return MethodReference.class.getSimpleName() + "{}";
        }
    }

    /**
     * Annotate one type of a cast instruction.
     *
     * Example:
     * <pre><code>(Serializable & @TestAnno Cloneable) obj</code></pre>
     */
    final class Cast implements InsnTargetType {
        public static final int SORT = 12;

        /**
         * Index of the intersected type that is annotated.
         *
         * Examples:
         * - 0 for <pre><code>(@TestAnno Serializable) obj</code></pre>
         * - 0 for <pre><code>(@TestAnno Serializable & Cloneable) obj</code></pre>
         * - 1 for <pre><code>(Serializable & @TestAnno Cloneable) obj</code></pre>
         */
        private int intersection;

        public Cast(int intersection) {
            this.intersection = intersection;
        }

        public int getIntersection() {
            return intersection;
        }

        public void setIntersection(int intersection) {
            this.intersection = intersection;
        }

        @Override
        public int getSort() {
            return 12;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cast cast = (Cast) o;
            return intersection == cast.intersection;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), intersection);
        }

        @Override
        public String toString() {
            return Cast.class.getSimpleName() + '{' +
                    "intersection=" + intersection +
                    '}';
        }
    }

    abstract class AbstractTypeParameterInsnTargetType implements InsnTargetType {
        /**
         * Index of the annotated type parameter
         */
        private int typeParameter;

        public AbstractTypeParameterInsnTargetType(int typeParameter) {
            this.typeParameter = typeParameter;
        }

        public int getTypeParameter() {
            return typeParameter;
        }

        public void setTypeParameter(int typeParameter) {
            this.typeParameter = typeParameter;
        }
    }

    /**
     * Annotate a type parameter of a constructor call.
     *
     * Example:
     * <pre><code>new <@MyAnno Bar>Foo()</code></pre>
     */
    final class ConstructorInvokeTypeParameter extends AbstractTypeParameterInsnTargetType {
        public static final int SORT = 13;

        public ConstructorInvokeTypeParameter(int typeParameter) {
            super(typeParameter);
        }

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConstructorInvokeTypeParameter that = (ConstructorInvokeTypeParameter) o;
            return getTypeParameter() == that.getTypeParameter();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), getTypeParameter());
        }

        @Override
        public String toString() {
            return ConstructorInvokeTypeParameter.class.getSimpleName() + '{' +
                    "typeParameter=" + getTypeParameter() +
                    '}';
        }
    }

    /**
     * Annotate a type parameter of a method call.
     *
     * Example:
     * <pre><code>this.<@MyAnno Bar>foo()</code></pre>
     */
    final class MethodInvokeTypeParameter extends AbstractTypeParameterInsnTargetType {
        public static final int SORT = 14;

        public MethodInvokeTypeParameter(int typeParameter) {
            super(typeParameter);
        }

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodInvokeTypeParameter that = (MethodInvokeTypeParameter) o;
            return getTypeParameter() == that.getTypeParameter();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), getTypeParameter());
        }

        @Override
        public String toString() {
            return MethodInvokeTypeParameter.class.getSimpleName() + '{' +
                    "typeParameter=" + getTypeParameter() +
                    '}';
        }
    }

    /**
     * Annotate a type parameter of a constructor reference.
     *
     * Example:
     * <pre><code>Foo::<@MyAnno Bar>new</code></pre>
     */
    final class ConstructorReferenceTypeParameter extends AbstractTypeParameterInsnTargetType {
        public static final int SORT = 15;

        public ConstructorReferenceTypeParameter(int typeParameter) {
            super(typeParameter);
        }

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConstructorReferenceTypeParameter that = (ConstructorReferenceTypeParameter) o;
            return getTypeParameter() == that.getTypeParameter();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), getTypeParameter());
        }

        @Override
        public String toString() {
            return ConstructorReferenceTypeParameter.class.getSimpleName() + '{' +
                    "typeParameter=" + getTypeParameter() +
                    '}';
        }
    }

    /**
     * Annotate a type parameter of a method reference.
     *
     * Example:
     * <pre><code>this::<@MyAnno Bar>foo"</code></pre>
     */
    final class MethodReferenceTypeParameter extends AbstractTypeParameterInsnTargetType {
        public static final int SORT = 16;

        public MethodReferenceTypeParameter(int typeParameter) {
            super(typeParameter);
        }

        @Override
        public int getSort() {
            return SORT;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodReferenceTypeParameter that = (MethodReferenceTypeParameter) o;
            return getTypeParameter() == that.getTypeParameter();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getSort(), getTypeParameter());
        }

        @Override
        public String toString() {
            return MethodReferenceTypeParameter.class.getSimpleName() + '{' +
                    "typeParameter=" + getTypeParameter() +
                    '}';
        }
    }
}
