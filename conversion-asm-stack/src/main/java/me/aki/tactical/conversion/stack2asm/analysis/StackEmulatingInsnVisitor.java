package me.aki.tactical.conversion.stack2asm.analysis;

import me.aki.tactical.conversion.stackasm.InsnVisitor;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.InvokableMethodRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.constant.Constant;
import me.aki.tactical.core.handle.BootstrapMethodHandle;
import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.insn.IfInsn;
import me.aki.tactical.stack.insn.InvokeInsn;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

public class StackEmulatingInsnVisitor<T> extends InsnVisitor<T> {
    private Stack.Mutable stack;

    public StackEmulatingInsnVisitor(InsnVisitor<T> iv, Stack.Mutable stack) {
        super(iv);
        this.stack = stack;
    }

    public Stack.Mutable getStack() {
        return stack;
    }

    public void setStack(Stack.Mutable stack) {
        this.stack = stack;
    }

    @Override
    public void visitPush(Constant constant) {
        this.stack.push(JvmType.from(constant.getType()));

        super.visitPush(constant);
    }

    @Override
    public void visitNeg(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // value to be negated
        this.stack.push(jvmType); // negated value

        super.visitNeg(type);
    }

    @Override
    public void visitAdd(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitAdd(type);
    }

    @Override
    public void visitSub(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitSub(type);
    }

    @Override
    public void visitMul(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitMul(type);
    }

    @Override
    public void visitDiv(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitDiv(type);
    }

    @Override
    public void visitMod(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitMod(type);
    }

    @Override
    public void visitAnd(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitAnd(type);
    }

    @Override
    public void visitOr(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitOr(type);
    }

    @Override
    public void visitXor(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitXor(type);
    }

    @Override
    public void visitShl(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(JvmType.INT); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitShl(type);
    }

    @Override
    public void visitShr(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(JvmType.INT); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitShr(type);
    }

    @Override
    public void visitUShr(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(JvmType.INT); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(jvmType); //result

        super.visitUShr(type);
    }

    @Override
    public void visitCmp() {
        this.stack.popRequire(JvmType.LONG); // operand 2
        this.stack.popRequire(JvmType.LONG); // operand 1
        this.stack.push(JvmType.INT); //result

        super.visitCmp();
    }

    @Override
    public void visitCmpl(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(JvmType.INT); //result

        super.visitCmpl(type);
    }

    @Override
    public void visitCmpg(Type type) {
        JvmType jvmType = JvmType.from(type);
        this.stack.popRequire(jvmType); // operand 2
        this.stack.popRequire(jvmType); // operand 1
        this.stack.push(JvmType.INT); //result

        super.visitCmpg(type);
    }

    @Override
    public void visitNewArray(ArrayType type, int initializedDimensions) {
        for (int i = 0; i < initializedDimensions; i++) {
            this.stack.popRequire(JvmType.INT); // size of a dimension
        }
        this.stack.push(JvmType.REFERENCE); // the created array

        super.visitNewArray(type, initializedDimensions);
    }

    @Override
    public void visitArrayLength() {
        this.stack.popRequire(JvmType.REFERENCE); // an array
        this.stack.push(JvmType.INT); // length of array

        super.visitArrayLength();
    }

    @Override
    public void visitArrayLoad(Type type) {
        this.stack.popRequire(JvmType.INT); // index
        this.stack.popRequire(JvmType.REFERENCE); // the array
        this.stack.push(JvmType.from(type)); // an element from the array

        super.visitArrayLoad(type);
    }

    @Override
    public void visitArrayStore(Type type) {
        this.stack.popRequire(JvmType.from(type)); // element to be stored
        this.stack.popRequire(JvmType.INT); // index
        this.stack.popRequire(JvmType.REFERENCE); // the array

        super.visitArrayStore(type);
    }

    @Override
    public void visitSwap() {
        JvmType value1 = this.stack.pop();
        JvmType value2 = this.stack.pop();
        this.stack.push(value1);
        this.stack.push(value2);

        super.visitSwap();
    }

    @Override
    public void visitPop() {
        this.stack.pop();

        super.visitPop();
    }

    @Override
    public void visitDup() {
        JvmType value = this.stack.pop();
        this.stack.push(value);
        this.stack.push(value);

        super.visitDup();
    }

    @Override
    public void visitDupX1() {
        JvmType value1 = this.stack.pop();
        JvmType value2 = this.stack.pop();

        this.stack.push(value1);

        this.stack.push(value2);
        this.stack.push(value1);

        super.visitDupX1();
    }

    @Override
    public void visitDupX2() {
        JvmType value1 = this.stack.pop();
        JvmType value2 = this.stack.pop();
        JvmType value3 = this.stack.pop();

        this.stack.push(value1);

        this.stack.push(value3);
        this.stack.push(value2);
        this.stack.push(value1);

        super.visitDupX2();
    }

    @Override
    public void visitDup2() {
        JvmType value1 = this.stack.pop();
        JvmType value2 = this.stack.pop();

        this.stack.push(value2);
        this.stack.push(value1);

        this.stack.push(value2);
        this.stack.push(value1);

        super.visitDup2();
    }

    @Override
    public void visitDup2X1() {
        JvmType value1 = this.stack.pop();
        JvmType value2 = this.stack.pop();
        JvmType value3 = this.stack.pop();

        this.stack.push(value2);
        this.stack.push(value1);

        this.stack.push(value3);
        this.stack.push(value2);
        this.stack.push(value1);

        super.visitDup2X1();
    }

    @Override
    public void visitDup2X2() {
        JvmType value1 = this.stack.pop();
        JvmType value2 = this.stack.pop();
        JvmType value3 = this.stack.pop();
        JvmType value4 = this.stack.pop();

        this.stack.push(value2);
        this.stack.push(value1);

        this.stack.push(value4);
        this.stack.push(value3);
        this.stack.push(value2);
        this.stack.push(value1);

        super.visitDup2X2();
    }

    @Override
    public void visitLoad(Type type, Local local) {
        this.stack.push(JvmType.from(type)); // value from local

        super.visitLoad(type, local);
    }

    @Override
    public void visitStore(Type type, Local local) {
        this.stack.popRequire(JvmType.from(type)); // value to be stored in local

        super.visitStore(type, local);
    }

    @Override
    public void visitIncrement(Local local, int value) {
        // no stack change

        super.visitIncrement(local, value);
    }

    @Override
    public void visitNew(Path type) {
        this.stack.push(JvmType.REFERENCE); // not yet initialized object

        super.visitNew(type);
    }

    @Override
    public void visitInstanceOf(RefType type) {
        this.stack.popRequire(JvmType.REFERENCE); // object to be checked
        this.stack.push(JvmType.INT); // result (0 or 1)

        super.visitInstanceOf(type);
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType from, PrimitiveType to) {
        this.stack.popRequire(JvmType.from(from)); // value to be casted
        this.stack.push(JvmType.from(to)); // casted value

        super.visitPrimitiveCast(from, to);
    }

    @Override
    public void visitReferenceCast(RefType type) {
        this.stack.popRequire(JvmType.REFERENCE); // value to be casted
        this.stack.push(JvmType.REFERENCE); // the casted value

        super.visitReferenceCast(type);
    }

    @Override
    public void visitReturn(Optional<Type> type) {
        type.map(JvmType::from)
                .ifPresent(this.stack::popRequire); // value to be returned

        super.visitReturn(type);
    }

    @Override
    public void visitThrow() {
        this.stack.popRequire(JvmType.REFERENCE); // exception to be thrown

        // Clear the stack and push the exception.
        // This prepares the stack for a possible jump to an exception handler.
        this.stack.clear();
        this.stack.push(JvmType.REFERENCE);

        super.visitThrow();
    }

    @Override
    public void visitMonitorEnter() {
        this.stack.popRequire(JvmType.REFERENCE); // value to gain lock on

        super.visitMonitorEnter();
    }

    @Override
    public void visitMonitorExit() {
        this.stack.popRequire(JvmType.REFERENCE); // value to release lock from

        super.visitMonitorExit();
    }

    @Override
    public void visitFieldGet(FieldRef fieldRef, boolean isStatic) {
        if (!isStatic) {
            this.stack.popRequire(JvmType.REFERENCE); // instance of class containing the field
        }
        this.stack.push(JvmType.from(fieldRef.getType())); // value of the field

        super.visitFieldGet(fieldRef, isStatic);
    }

    @Override
    public void visitFieldSet(FieldRef fieldRef, boolean isStatic) {
        this.stack.popRequire(JvmType.from(fieldRef.getType())); // value to store in field
        if (!isStatic) {
            this.stack.popRequire(JvmType.REFERENCE); // instance of class containing the field
        }

        super.visitFieldSet(fieldRef, isStatic);
    }

    @Override
    public void visitInvokeInsn(InvokeInsn.InvokeType invoke, InvokableMethodRef method) {
        // pop arguments in reverse order
        List<Type> arguments = method.getArguments();
        ListIterator<Type> iter = arguments.listIterator(arguments.size());
        while (iter.hasPrevious()) {
            this.stack.popRequire(JvmType.from(iter.previous()));
        }

        if (invoke != InvokeInsn.InvokeType.STATIC) {
            this.stack.popRequire(JvmType.REFERENCE); // instance of class containing the method
        }

        super.visitInvokeInsn(invoke, method);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, MethodDescriptor descriptor, BootstrapMethodHandle bootstrapMethod, List<BootstrapConstant> bootstrapArguments) {
        // pop arguments in reverse order
        List<Type> paramTypes = descriptor.getParameterTypes();
        ListIterator<Type> iter = paramTypes.listIterator(paramTypes.size());
        while (iter.hasPrevious()) {
            this.stack.popRequire(JvmType.from(iter.previous()));
        }

        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethod, bootstrapArguments);
    }

    @Override
    public void visitGoto(T target) {
        // no stack changes

        super.visitGoto(target);
    }

    @Override
    public void visitIf(IfInsn.Condition condition, T target) {
        JvmType type = condition instanceof IfInsn.IntCondition ? JvmType.INT : JvmType.REFERENCE;
        if (condition.getCompareValue() instanceof IfInsn.StackValue) {
            this.stack.popRequire(type); // other value from stack to compare against
        }
        this.stack.popRequire(type); // value to be compared

        super.visitIf(condition, target);
    }

    @Override
    public void visitSwitch(Map<Integer, T> targetTable, T defaultTarget) {
        this.stack.popRequire(JvmType.INT); // value to be compared against the table

        super.visitSwitch(targetTable, defaultTarget);
    }
}
