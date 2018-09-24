package me.aki.tactical.core;

import java.util.Objects;

public interface MethodHandle {
    abstract class AbstractFieldHandle implements MethodHandle {
        private final FieldRef fieldRef;

        public AbstractFieldHandle(FieldRef fieldRef) {
            this.fieldRef = fieldRef;
        }

        public FieldRef getFieldRef() {
            return fieldRef;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AbstractFieldHandle that = (AbstractFieldHandle) o;
            return Objects.equals(fieldRef, that.fieldRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldRef);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '{' +
                    "fieldRef=" + fieldRef +
                    '}';
        }
    }

    abstract class AbstractMethodHandle implements MethodHandle {
        private final MethodRef methodRef;

        public AbstractMethodHandle(MethodRef methodRef) {
            this.methodRef = methodRef;
        }

        public MethodRef getMethodRef() {
            return methodRef;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AbstractMethodHandle that = (AbstractMethodHandle) o;
            return Objects.equals(methodRef, that.methodRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(methodRef);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '{' +
                    "methodRef=" + methodRef +
                    '}';
        }
    }

    /**
     * Method Handle where the containing class might be either an interface or a class.
     */
    abstract class AbstractAmbiguousMethodHandle extends AbstractMethodHandle {
        /**
         * Is the class that contains the method an interface
         */
        private boolean isInterface;

        public AbstractAmbiguousMethodHandle(MethodRef methodRef, boolean isInterface) {
            super(methodRef);
            this.isInterface = isInterface;
        }

        public boolean isInterface() {
            return isInterface;
        }

        public void setInterface(boolean anInterface) {
            isInterface = anInterface;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            AbstractAmbiguousMethodHandle that = (AbstractAmbiguousMethodHandle) o;
            return isInterface == that.isInterface;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), isInterface);
        }

        @Override
        public String toString() {
            return AbstractAmbiguousMethodHandle.class.getSimpleName() + '{' +
                    "methodRef=" + getMethodRef() +
                    ", isInterface=" + isInterface +
                    '}';
        }
    }

    class NewInstanceHandle extends AbstractMethodHandle {
        public NewInstanceHandle(MethodRef methodRef) {
            super(methodRef);
        }
    }

    class InvokeStaticHandle extends AbstractAmbiguousMethodHandle {
        public InvokeStaticHandle(MethodRef methodRef, boolean isInterface) {
            super(methodRef, isInterface);
        }
    }

    class InvokeSpecialHandle extends AbstractAmbiguousMethodHandle {
        public InvokeSpecialHandle(MethodRef methodRef, boolean isInterface) {
            super(methodRef, isInterface);
        }
    }

    class InvokeInterfaceHandle extends AbstractMethodHandle {
        public InvokeInterfaceHandle(MethodRef methodRef) {
            super(methodRef);
        }
    }

    class InvokeVirtualHandle extends AbstractMethodHandle {
        public InvokeVirtualHandle(MethodRef methodRef) {
            super(methodRef);
        }
    }

    class GetFieldHandle extends AbstractFieldHandle {
        public GetFieldHandle(FieldRef fieldRef) {
            super(fieldRef);
        }
    }

    class SetFieldHandle extends AbstractFieldHandle {
        public SetFieldHandle(FieldRef fieldRef) {
            super(fieldRef);
        }
    }

    class GetStaticHandle extends AbstractFieldHandle {
        public GetStaticHandle(FieldRef fieldRef) {
            super(fieldRef);
        }
    }

    class SetStaticHandle extends AbstractFieldHandle {
        public SetStaticHandle(FieldRef fieldRef) {
            super(fieldRef);
        }
    }
}
