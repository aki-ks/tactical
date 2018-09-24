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
import me.aki.tactical.core.type.BooleanType;
import me.aki.tactical.core.type.ByteType;
import me.aki.tactical.core.type.CharType;
import me.aki.tactical.core.type.DoubleType;
import me.aki.tactical.core.type.FloatType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.ShortType;
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
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

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
        int opcode = type instanceof IntType ? Opcodes.INEG :
                type instanceof LongType ? Opcodes.LNEG :
                type instanceof FloatType ? Opcodes.FNEG :
                type instanceof DoubleType ? Opcodes.DNEG :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitAdd(Type type) {
        int opcode = type instanceof IntType ? Opcodes.IADD :
                type instanceof LongType ? Opcodes.LADD :
                type instanceof FloatType ? Opcodes.FADD :
                type instanceof DoubleType ? Opcodes.DADD :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitSub(Type type) {
        int opcode = type instanceof IntType ? Opcodes.ISUB :
                type instanceof LongType ? Opcodes.LSUB :
                type instanceof FloatType ? Opcodes.FSUB :
                type instanceof DoubleType ? Opcodes.DSUB :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitMul(Type type) {
        int opcode = type instanceof IntType ? Opcodes.IMUL :
                type instanceof LongType ? Opcodes.LMUL :
                type instanceof FloatType ? Opcodes.FMUL :
                type instanceof DoubleType ? Opcodes.DMUL :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitDiv(Type type) {
        int opcode = type instanceof IntType ? Opcodes.IDIV :
                type instanceof LongType ? Opcodes.LDIV :
                type instanceof FloatType ? Opcodes.FDIV :
                type instanceof DoubleType ? Opcodes.DDIV :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitMod(Type type) {
        int opcode = type instanceof IntType ? Opcodes.IREM :
                type instanceof LongType ? Opcodes.LREM :
                type instanceof FloatType ? Opcodes.FREM :
                type instanceof DoubleType ? Opcodes.DREM :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitAnd(Type type) {
        int opcode = type instanceof IntType ? Opcodes.IAND :
                type instanceof LongType ? Opcodes.LAND :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitOr(Type type) {
        int opcode = type instanceof IntType ? Opcodes.IOR :
                type instanceof LongType ? Opcodes.LOR :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitXor(Type type) {
        int opcode = type instanceof IntType ? Opcodes.IXOR :
                type instanceof LongType ? Opcodes.LXOR :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitShl(Type type) {
        int opcode = type instanceof IntType ? Opcodes.ISHL :
                type instanceof LongType ? Opcodes.LSHL :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitShr(Type type) {
        int opcode = type instanceof IntType ? Opcodes.ISHR :
                type instanceof LongType ? Opcodes.LSHR :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitUShr(Type type) {
        int opcode = type instanceof IntType ? Opcodes.IUSHR :
                type instanceof LongType ? Opcodes.LUSHR :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitCmp() {
        visitConvertedInsn(new InsnNode(Opcodes.LCMP));
    }

    @Override
    public void visitCmpl(Type type) {
        int opcode = type instanceof FloatType ? Opcodes.FCMPL :
                type instanceof DoubleType ? Opcodes.DCMPL :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitCmpg(Type type) {
        int opcode = type instanceof FloatType ? Opcodes.FCMPG :
                type instanceof DoubleType ? Opcodes.DCMPG :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitNewArray(ArrayType type, int initializedDimensions) {
        if (initializedDimensions == 1) {
            Type baseType = type.getBaseType();
            if (baseType instanceof RefType) {
                String descriptor = AsmUtil.toInternalName((RefType) baseType);
                visitConvertedInsn(new TypeInsnNode(Opcodes.ANEWARRAY, descriptor));
            } else {
                int operand = baseType instanceof BooleanType ? Opcodes.T_BOOLEAN :
                        baseType instanceof ByteType ? Opcodes.T_BYTE :
                        baseType instanceof CharType ? Opcodes.T_CHAR :
                        baseType instanceof ShortType ? Opcodes.T_SHORT :
                        baseType instanceof IntType ? Opcodes.T_INT :
                        baseType instanceof LongType ? Opcodes.T_LONG :
                        baseType instanceof FloatType ? Opcodes.T_FLOAT :
                        baseType instanceof DoubleType ? Opcodes.T_DOUBLE :
                        assertionError();

                visitConvertedInsn(new IntInsnNode(Opcodes.NEWARRAY, operand));
            }
        } else {
            String descriptor = AsmUtil.toDescriptor(type);
            visitConvertedInsn(new MultiANewArrayInsnNode(descriptor, initializedDimensions));
        }
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
