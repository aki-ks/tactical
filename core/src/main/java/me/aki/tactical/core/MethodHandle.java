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

    class NewInstanceHandle extends AbstractMethodHandle {
        public NewInstanceHandle(MethodRef methodRef) {
            super(methodRef);
        }
    }

    class InvokeStaticHandle extends AbstractMethodHandle {
        public InvokeStaticHandle(MethodRef methodRef) {
            super(methodRef);
        }
    }

    class InvokeSpecialHandle extends AbstractMethodHandle {
        public InvokeSpecialHandle(MethodRef methodRef) {
            super(methodRef);
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
