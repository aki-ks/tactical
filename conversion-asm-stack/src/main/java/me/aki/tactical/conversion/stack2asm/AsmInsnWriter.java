package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stackasm.InsnVisitor;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodHandle;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.Path;
import me.aki.tactical.core.constant.BootstrapConstant;
import me.aki.tactical.core.constant.ClassConstant;
import me.aki.tactical.core.constant.Constant;
import me.aki.tactical.core.constant.DoubleConstant;
import me.aki.tactical.core.constant.FloatConstant;
import me.aki.tactical.core.constant.IntConstant;
import me.aki.tactical.core.constant.LongConstant;
import me.aki.tactical.core.constant.MethodHandleConstant;
import me.aki.tactical.core.constant.MethodTypeConstant;
import me.aki.tactical.core.constant.NullConstant;
import me.aki.tactical.core.constant.StringConstant;
import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.stack.InvokableMethodRef;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.insn.IfInsn;
import me.aki.tactical.stack.insn.Instruction;
import me.aki.tactical.stack.insn.InvokeInsn;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AsmInsnWriter extends InsnVisitor.Tactical {
    private final List<AbstractInsnNode> convertedInsns = new ArrayList<>();

    public AsmInsnWriter() {
        super(null);
    }

    private void visitConvertedInsn(AbstractInsnNode node) {
        convertedInsns.add(node);
    }

    private <T> T assertionError() {
        throw new AssertionError();
    }

    @Override
    public void visitPush(Constant constant) {
        if (constant instanceof IntConstant) {
            int value = ((IntConstant) constant).getValue();
            if (value >= -1 && value <= 5) {
                visitConvertedInsn(new InsnNode(Opcodes.ICONST_0 + value));
            } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                visitConvertedInsn(new IntInsnNode(Opcodes.BIPUSH, value));
            } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                visitConvertedInsn(new IntInsnNode(Opcodes.SIPUSH, value));
            }
        } else if (constant instanceof LongConstant) {
            long value = ((LongConstant) constant).getValue();
            if (value == 0) {
                visitConvertedInsn(new InsnNode(Opcodes.LCONST_0));
            } else if (value == 1) {
                visitConvertedInsn(new InsnNode(Opcodes.LCONST_1));
            } else {
                visitConvertedInsn(new LdcInsnNode(value));
            }
        } else if (constant instanceof FloatConstant) {
            float value = ((FloatConstant) constant).getValue();
            if (value == 0) {
                visitConvertedInsn(new InsnNode(Opcodes.FCONST_0));
            } else if (value == 1) {
                visitConvertedInsn(new InsnNode(Opcodes.FCONST_1));
            } else if (value == 2) {
                visitConvertedInsn(new InsnNode(Opcodes.FCONST_2));
            } else {
                visitConvertedInsn(new LdcInsnNode(value));
            }
        } else if (constant instanceof DoubleConstant) {
            double value = ((DoubleConstant) constant).getValue();
            if (value == 0) {
                visitConvertedInsn(new InsnNode(Opcodes.DCONST_0));
            } else if (value == 1) {
                visitConvertedInsn(new InsnNode(Opcodes.DCONST_1));
            } else {
                visitConvertedInsn(new LdcInsnNode(value));
            }
        } else if (constant instanceof StringConstant) {
            String string = ((StringConstant) constant).getValue();
            visitConvertedInsn(new LdcInsnNode(string));
        } else if (constant instanceof NullConstant) {
            visitConvertedInsn(new InsnNode(Opcodes.ACONST_NULL));
        } else if (constant instanceof ClassConstant) {
            Type value = ((ClassConstant) constant).getValue();
            visitConvertedInsn(new LdcInsnNode(AsmUtil.toAsmType(value)));
        } else if (constant instanceof MethodTypeConstant) {
            MethodTypeConstant methodConstant = (MethodTypeConstant) constant;
            visitConvertedInsn(new LdcInsnNode(AsmUtil.methodDescriptorToType(
                    methodConstant.getReturnType(), methodConstant.getArgumentTypes())));
        } else if (constant instanceof MethodHandleConstant) {
            MethodHandle handle = ((MethodHandleConstant) constant).getHandle();

            int type;
            String owner;
            String name;
            String desc;
            boolean isInterface;
            if (handle instanceof MethodHandle.AbstractFieldHandle) {
                FieldRef field = ((MethodHandle.AbstractFieldHandle) handle).getFieldRef();
                owner = field.getOwner().join('/');
                name = field.getName();
                desc = AsmUtil.toDescriptor(field.getType());
                isInterface = false;

                if (handle instanceof MethodHandle.GetFieldHandle) {
                    type = Opcodes.H_GETFIELD;
                } else if (handle instanceof MethodHandle.GetStaticHandle) {
                    type = Opcodes.H_GETSTATIC;
                } else if (handle instanceof MethodHandle.SetFieldHandle) {
                    type = Opcodes.H_PUTFIELD;
                } else if (handle instanceof MethodHandle.SetStaticHandle) {
                    type = Opcodes.H_PUTSTATIC;
                } else {
                    throw new AssertionError();
                }
            } else if (handle instanceof MethodHandle.AbstractMethodHandle) {
                MethodRef method = ((MethodHandle.AbstractMethodHandle) handle).getMethodRef();
                owner = method.getOwner().join('/');
                name = method.getName();
                desc = AsmUtil.methodDescriptorToString(method.getReturnType(), method.getArguments());
                isInterface = handle instanceof MethodHandle.InvokeInterfaceHandle ||
                        handle instanceof MethodHandle.AbstractAmbiguousMethodHandle &&
                                ((MethodHandle.AbstractAmbiguousMethodHandle) handle).isInterface();

                if (handle instanceof MethodHandle.InvokeInterfaceHandle) {
                    type = Opcodes.H_INVOKEINTERFACE;
                } else if (handle instanceof MethodHandle.InvokeSpecialHandle) {
                    type = Opcodes.H_INVOKESPECIAL;
                } else if (handle instanceof MethodHandle.InvokeStaticHandle) {
                    type = Opcodes.H_INVOKESTATIC;
                } else if (handle instanceof MethodHandle.InvokeVirtualHandle) {
                    type = Opcodes.H_INVOKEVIRTUAL;
                } else if (handle instanceof MethodHandle.NewInstanceHandle) {
                    type = Opcodes.H_NEWINVOKESPECIAL;
                } else {
                    throw new AssertionError();
                }
            } else {
                throw new AssertionError();
            }

            visitConvertedInsn(new LdcInsnNode(new Handle(type, owner, name, desc, isInterface)));
        }
    }

    @Override
    public void visitNeg(Type type) {
        super.visitNeg(type);
    }

    @Override
    public void visitAdd(Type type) {
        super.visitAdd(type);
    }

    @Override
    public void visitSub(Type type) {
        super.visitSub(type);
    }

    @Override
    public void visitMul(Type type) {
        super.visitMul(type);
    }

    @Override
    public void visitDiv(Type type) {
        super.visitDiv(type);
    }

    @Override
    public void visitMod(Type type) {
        super.visitMod(type);
    }

    @Override
    public void visitAnd(Type type) {
        super.visitAnd(type);
    }

    @Override
    public void visitOr(Type type) {
        super.visitOr(type);
    }

    @Override
    public void visitXor(Type type) {
        super.visitXor(type);
    }

    @Override
    public void visitShl(Type type) {
        super.visitShl(type);
    }

    @Override
    public void visitShr(Type type) {
        super.visitShr(type);
    }

    @Override
    public void visitUShr(Type type) {
        super.visitUShr(type);
    }

    @Override
    public void visitCmp() {
        super.visitCmp();
    }

    @Override
    public void visitCmpl(Type type) {
        super.visitCmpl(type);
    }

    @Override
    public void visitCmpg(Type type) {
        super.visitCmpg(type);
    }

    @Override
    public void visitNewArray(ArrayType type, int initializedDimensions) {
        super.visitNewArray(type, initializedDimensions);
    }

    @Override
    public void visitArrayLength() {
        super.visitArrayLength();
    }

    @Override
    public void visitArrayLoad(Type type) {
        super.visitArrayLoad(type);
    }

    @Override
    public void visitArrayStore(Type type) {
        super.visitArrayStore(type);
    }

    @Override
    public void visitSwap() {
        super.visitSwap();
    }

    @Override
    public void visitPop() {
        super.visitPop();
    }

    @Override
    public void visitDup() {
        super.visitDup();
    }

    @Override
    public void visitDupX1() {
        super.visitDupX1();
    }

    @Override
    public void visitDupX2() {
        super.visitDupX2();
    }

    @Override
    public void visitDup2() {
        super.visitDup2();
    }

    @Override
    public void visitDup2X1() {
        super.visitDup2X1();
    }

    @Override
    public void visitDup2X2() {
        super.visitDup2X2();
    }

    @Override
    public void visitLoad(Type type, Local local) {
        super.visitLoad(type, local);
    }

    @Override
    public void visitStore(Type type, Local local) {
        super.visitStore(type, local);
    }

    @Override
    public void visitIncrement(Local local, int value) {
        super.visitIncrement(local, value);
    }

    @Override
    public void visitNew(Path type) {
        super.visitNew(type);
    }

    @Override
    public void visitInstanceOf(RefType type) {
        super.visitInstanceOf(type);
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType from, PrimitiveType to) {
        super.visitPrimitiveCast(from, to);
    }

    @Override
    public void visitReferenceCast(RefType type) {
        super.visitReferenceCast(type);
    }

    @Override
    public void visitReturn(Optional<Type> type) {
        super.visitReturn(type);
    }

    @Override
    public void visitThrow() {
        super.visitThrow();
    }

    @Override
    public void visitMonitorEnter() {
        super.visitMonitorEnter();
    }

    @Override
    public void visitMonitorExit() {
        super.visitMonitorExit();
    }

    @Override
    public void visitFieldGet(FieldRef fieldRef, boolean isStatic) {
        super.visitFieldGet(fieldRef, isStatic);
    }

    @Override
    public void visitFieldSet(FieldRef fieldRef, boolean isStatic) {
        super.visitFieldSet(fieldRef, isStatic);
    }

    @Override
    public void visitInvokeInsn(InvokeInsn.InvokeType invoke, InvokableMethodRef method) {
        super.visitInvokeInsn(invoke, method);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, MethodDescriptor descriptor, MethodRef bootstrapMethod, List<BootstrapConstant> bootstrapArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethod, bootstrapArguments);
    }

    @Override
    public void visitGoto(Instruction target) {
        super.visitGoto(target);
    }

    @Override
    public void visitIf(IfInsn.Condition condition, Instruction target) {
        super.visitIf(condition, target);
    }

    @Override
    public void visitSwitch(Map<Integer, Instruction> targetTable, Instruction defaultTarget) {
        super.visitSwitch(targetTable, defaultTarget);
    }
}
