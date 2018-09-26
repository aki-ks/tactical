package me.aki.tactical.conversion.stack2asm;

import me.aki.tactical.conversion.stack2asm.analysis.JvmType;
import me.aki.tactical.conversion.stack2asm.analysis.Stack;
import me.aki.tactical.conversion.stackasm.InsnVisitor;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.handle.AbstractAmbiguousMethodHandle;
import me.aki.tactical.core.handle.BootstrapMethodHandle;
import me.aki.tactical.core.handle.FieldHandle;
import me.aki.tactical.core.handle.GetFieldHandle;
import me.aki.tactical.core.handle.GetStaticHandle;
import me.aki.tactical.core.handle.Handle;
import me.aki.tactical.core.MethodDescriptor;
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
import me.aki.tactical.core.handle.InvokeInterfaceHandle;
import me.aki.tactical.core.handle.InvokeSpecialHandle;
import me.aki.tactical.core.handle.InvokeStaticHandle;
import me.aki.tactical.core.handle.InvokeVirtualHandle;
import me.aki.tactical.core.handle.MethodHandle;
import me.aki.tactical.core.handle.NewInstanceHandle;
import me.aki.tactical.core.handle.SetFieldHandle;
import me.aki.tactical.core.handle.SetStaticHandle;
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
import me.aki.tactical.core.InvokableMethodRef;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.insn.IfInsn;
import me.aki.tactical.stack.insn.Instruction;
import me.aki.tactical.stack.insn.InvokeInsn;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class AsmInsnWriter extends InsnVisitor<Instruction> {
    private final ConversionContext ctx;
    private final List<AbstractInsnNode> convertedInsns = new ArrayList<>();

    /**
     * The state of the stack before the next visited instruction
     */
    private Stack stackFrame;

    /**
     * A list of locals used to convert dup instructions that cannot be represented as one opcode.
     */
    private List<Local> tempLocalIndices = new ArrayList<>();

    public AsmInsnWriter(ConversionContext ctx) {
        super(null);
        this.ctx = ctx;
    }

    private void visitConvertedInsn(AbstractInsnNode node) {
        convertedInsns.add(node);
    }

    public List<AbstractInsnNode> getConvertedInsns() {
        return convertedInsns;
    }

    public void setStackFrame(Stack stackFrame) {
        this.stackFrame = stackFrame;
    }

    private Local getTempLocal(int i) {
        while (i <= tempLocalIndices.size()) {
            Local tempLocal = new Local();
            tempLocalIndices.add(tempLocal);
        }

        return tempLocalIndices.get(i);
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
            Handle handle = ((MethodHandleConstant) constant).getHandle();
            visitConvertedInsn(new LdcInsnNode(convertHandle(handle)));
        }
    }

    private org.objectweb.asm.Handle convertHandle(Handle handle) {
        int type;
        String owner;
        String name;
        String desc;
        boolean isInterface;
        if (handle instanceof FieldHandle) {
            FieldRef field = ((FieldHandle) handle).getFieldRef();
            owner = field.getOwner().join('/');
            name = field.getName();
            desc = AsmUtil.toDescriptor(field.getType());
            isInterface = false;

            if (handle instanceof GetFieldHandle) {
                type = Opcodes.H_GETFIELD;
            } else if (handle instanceof GetStaticHandle) {
                type = Opcodes.H_GETSTATIC;
            } else if (handle instanceof SetFieldHandle) {
                type = Opcodes.H_PUTFIELD;
            } else if (handle instanceof SetStaticHandle) {
                type = Opcodes.H_PUTSTATIC;
            } else {
                throw new AssertionError();
            }
        } else if (handle instanceof MethodHandle) {
            MethodRef method = ((MethodHandle) handle).getMethodRef();
            owner = method.getOwner().join('/');
            name = method.getName();
            desc = AsmUtil.methodDescriptorToString(method.getReturnType(), method.getArguments());
            isInterface = handle instanceof InvokeInterfaceHandle ||
                    handle instanceof AbstractAmbiguousMethodHandle &&
                            ((AbstractAmbiguousMethodHandle) handle).getMethodRef().isInterface();

            if (handle instanceof InvokeInterfaceHandle) {
                type = Opcodes.H_INVOKEINTERFACE;
            } else if (handle instanceof InvokeSpecialHandle) {
                type = Opcodes.H_INVOKESPECIAL;
            } else if (handle instanceof InvokeStaticHandle) {
                type = Opcodes.H_INVOKESTATIC;
            } else if (handle instanceof InvokeVirtualHandle) {
                type = Opcodes.H_INVOKEVIRTUAL;
            } else if (handle instanceof NewInstanceHandle) {
                type = Opcodes.H_NEWINVOKESPECIAL;
            } else {
                throw new AssertionError();
            }
        } else {
            throw new AssertionError();
        }

        return new org.objectweb.asm.Handle(type, owner, name, desc, isInterface);
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
        visitConvertedInsn(new InsnNode(Opcodes.ARRAYLENGTH));
    }

    @Override
    public void visitArrayLoad(Type type) {
        int opcode = type instanceof RefType ? Opcodes.AALOAD :
                type instanceof ByteType ? Opcodes.BALOAD :
                type instanceof ShortType ? Opcodes.SALOAD :
                type instanceof CharType ? Opcodes.CALOAD :
                type instanceof IntType ? Opcodes.IALOAD :
                type instanceof LongType ? Opcodes.LALOAD :
                type instanceof FloatType ? Opcodes.FALOAD :
                type instanceof DoubleType ? Opcodes.DALOAD :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitArrayStore(Type type) {
        int opcode = type instanceof RefType ? Opcodes.AASTORE :
                type instanceof ByteType ? Opcodes.BASTORE :
                type instanceof ShortType ? Opcodes.SASTORE :
                type instanceof CharType ? Opcodes.CASTORE :
                type instanceof IntType ? Opcodes.IASTORE :
                type instanceof LongType ? Opcodes.LASTORE :
                type instanceof FloatType ? Opcodes.FASTORE :
                type instanceof DoubleType ? Opcodes.DASTORE :
                assertionError();

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitSwap() {
        JvmType[] peeked = this.stackFrame.peek(2);
        Integer opcode = null;
        if (peeked[0].is32bit()) {
            if (peeked[1].is32bit()) {
                opcode = Opcodes.SWAP;
            }
        }

        if (opcode == null) {
            Local local0 = getTempLocal(0);
            Local local1 = getTempLocal(1);

            visitStore(peeked[0].toType(), local0);
            visitStore(peeked[1].toType(), local1);

            visitLoad(peeked[0].toType(), local0);
            visitLoad(peeked[1].toType(), local1);
        } else {
            visitConvertedInsn(new InsnNode(opcode));
        }
    }

    @Override
    public void visitPop() {
        int opcode;
        if (this.stackFrame.peek().is32bit()) {
            opcode = Opcodes.POP;
        } else {
            opcode = Opcodes.POP2;
        }

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitDup() {
        int opcode;
        if (this.stackFrame.peek().is32bit()) {
            opcode = Opcodes.DUP;
        } else {
            opcode = Opcodes.DUP2;
        }

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitDupX1() {
        JvmType[] peeked = this.stackFrame.peek(2);
        int opcode;
        if (peeked[0].is32bit()) {
            if (peeked[1].is32bit()) {
                // DUP2_X1 Form 1
                opcode = Opcodes.DUP_X1;
            } else {
                // DUP_X2 Form 2
                opcode = Opcodes.DUP_X2;
            }
        } else {
            if (peeked[1].is32bit()) {
                // DUP2_X1 Form 2
                opcode = Opcodes.DUP2_X1;
            } else {
                // DUP2_X2 Form 4
                opcode = Opcodes.DUP2_X2;
            }
        }

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitDupX2() {
        JvmType[] peeked = this.stackFrame.peek(3);
        Integer opcode = null;
        if (peeked[0].is32bit()) {
            if (peeked[1].is32bit()) {
                if (peeked[2].is32bit()) {
                    // DUP_X2 Form 1
                    opcode = Opcodes.DUP_X2;
                }
            }
        } else {
            if (peeked[1].is32bit()) {
                if (peeked[2].is32bit()) {
                    // DUP2_X2 Form 2
                    opcode = Opcodes.DUP2_X2;
                }
            }
        }

        if (opcode == null) {
            Local local0 = getTempLocal(0);
            Local local1 = getTempLocal(1);
            Local local2 = getTempLocal(2);

            visitStore(peeked[0].toType(), local0);
            visitStore(peeked[1].toType(), local1);
            visitStore(peeked[2].toType(), local2);

            visitLoad(peeked[0].toType(), local0);

            visitLoad(peeked[2].toType(), local2);
            visitLoad(peeked[1].toType(), local1);
            visitLoad(peeked[0].toType(), local0);
        } else {
            visitConvertedInsn(new InsnNode(opcode));
        }
    }

    @Override
    public void visitDup2() {
        JvmType[] peeked = this.stackFrame.peek(2);
        Integer opcode = null;
        if (peeked[0].is32bit() && peeked[1].is32bit()) {
            // DUP2 Form 1
            opcode = Opcodes.DUP2;
        }

        if (opcode == null) {
            Local local0 = getTempLocal(0);
            Local local1 = getTempLocal(1);

            visitStore(peeked[0].toType(), local0);
            visitStore(peeked[1].toType(), local1);

            visitLoad(peeked[1].toType(), local1);
            visitLoad(peeked[0].toType(), local0);

            visitLoad(peeked[1].toType(), local1);
            visitLoad(peeked[0].toType(), local0);
        } else {
            visitConvertedInsn(new InsnNode(opcode));
        }
    }

    @Override
    public void visitDup2X1() {
        JvmType[] peeked = this.stackFrame.peek(3);
        Integer opcode = null;
        if (peeked[0].is32bit()) {
            if (peeked[1].is32bit()) {
                if (peeked[2].is32bit()) {
                    // DUP2_X1 Form 1
                    opcode = Opcodes.DUP2_X1;
                } else {
                    // DUP2_X2 Form 3
                    opcode = Opcodes.DUP2_X2;
                }
            }
        }

        if (opcode == null) {
            Local local0 = getTempLocal(0);
            Local local1 = getTempLocal(1);
            Local local2 = getTempLocal(2);

            visitStore(peeked[0].toType(), local0);
            visitStore(peeked[1].toType(), local1);
            visitStore(peeked[2].toType(), local2);

            visitLoad(peeked[1].toType(), local1);
            visitLoad(peeked[0].toType(), local0);

            visitLoad(peeked[2].toType(), local2);
            visitLoad(peeked[1].toType(), local1);
            visitLoad(peeked[0].toType(), local0);
        } else {
            visitConvertedInsn(new InsnNode(opcode));
        }
    }

    @Override
    public void visitDup2X2() {
        JvmType[] peeked = this.stackFrame.peek(4);
        Integer opcode = null;
        if (peeked[0].is32bit()) {
            if (peeked[1].is32bit()) {
                if (peeked[2].is32bit()) {
                    if (peeked[3].is32bit()) {
                        opcode = Opcodes.DUP2_X2;
                    }
                }
            }
        }

        if (opcode == null) {
            Local local0 = getTempLocal(0);
            Local local1 = getTempLocal(1);
            Local local2 = getTempLocal(2);
            Local local3 = getTempLocal(3);

            visitStore(peeked[0].toType(), local0);
            visitStore(peeked[1].toType(), local1);
            visitStore(peeked[2].toType(), local2);
            visitStore(peeked[3].toType(), local3);

            visitLoad(peeked[1].toType(), local1);
            visitLoad(peeked[0].toType(), local0);

            visitLoad(peeked[3].toType(), local3);
            visitLoad(peeked[2].toType(), local2);
            visitLoad(peeked[1].toType(), local1);
            visitLoad(peeked[0].toType(), local0);
        } else {
            visitConvertedInsn(new InsnNode(opcode));
        }
    }

    @Override
    public void visitLoad(Type type, Local local) {
        int opcode = type instanceof RefType ? Opcodes.ALOAD :
                type instanceof IntType ? Opcodes.ILOAD :
                type instanceof LongType ? Opcodes.LLOAD :
                type instanceof FloatType ? Opcodes.FLOAD :
                type instanceof DoubleType ? Opcodes.DLOAD :
                assertionError();

        visitConvertedInsn(new VarInsnNode(opcode, ctx.getLocalIndex(local)));
    }

    @Override
    public void visitStore(Type type, Local local) {
        int opcode = type instanceof RefType ? Opcodes.ASTORE :
                type instanceof IntType ? Opcodes.ISTORE :
                type instanceof LongType ? Opcodes.LSTORE :
                type instanceof FloatType ? Opcodes.FSTORE :
                type instanceof DoubleType ? Opcodes.DSTORE :
                assertionError();

        visitConvertedInsn(new VarInsnNode(opcode, ctx.getLocalIndex(local)));
    }

    @Override
    public void visitIncrement(Local local, int value) {
        visitConvertedInsn(new IincInsnNode(ctx.getLocalIndex(local), value));
    }

    @Override
    public void visitNew(Path type) {
        visitConvertedInsn(new TypeInsnNode(Opcodes.NEW, type.join('/')));
    }

    @Override
    public void visitInstanceOf(RefType type) {
        visitConvertedInsn(new TypeInsnNode(Opcodes.INSTANCEOF, AsmUtil.toInternalName(type)));
    }

    @Override
    public void visitPrimitiveCast(PrimitiveType from, PrimitiveType to) {
        int opcode;
        if (from instanceof IntType) {
            opcode = to instanceof ByteType ? Opcodes.I2B :
                    to instanceof CharType ? Opcodes.I2C :
                    to instanceof ShortType ? Opcodes.I2S :
                    to instanceof LongType ? Opcodes.I2L :
                    to instanceof FloatType ? Opcodes.I2F :
                    to instanceof DoubleType ? Opcodes.I2D :
                    assertionError();
        } else if (from instanceof LongType) {
            opcode = to instanceof IntType ? Opcodes.L2I :
                    to instanceof FloatType ? Opcodes.L2F :
                    to instanceof DoubleType ? Opcodes.L2D :
                    assertionError();
        } else if (from instanceof FloatType) {
            opcode = to instanceof IntType ? Opcodes.F2I :
                    to instanceof LongType ? Opcodes.F2L :
                    to instanceof DoubleType ? Opcodes.F2D :
                    assertionError();
        } else if (from instanceof DoubleType) {
            opcode = to instanceof IntType ? Opcodes.D2I :
                    to instanceof LongType ? Opcodes.D2L :
                    to instanceof FloatType ? Opcodes.D2F :
                    assertionError();
        } else {
            throw new AssertionError();
        }

        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitReferenceCast(RefType type) {
        visitConvertedInsn(new TypeInsnNode(Opcodes.CHECKCAST, AsmUtil.toInternalName(type)));
    }

    @Override
    public void visitReturn(Optional<Type> typeOpt) {
        int opcode;
        if (typeOpt.isPresent()) {
            Type type = typeOpt.get();
            opcode = type instanceof RefType ? Opcodes.ARETURN :
                    type instanceof IntType ? Opcodes.IRETURN :
                    type instanceof LongType ? Opcodes.LRETURN :
                    type instanceof FloatType ? Opcodes.FRETURN :
                    type instanceof DoubleType ? Opcodes.DRETURN :
                    assertionError();
        } else {
            opcode = Opcodes.RETURN;
        }
        visitConvertedInsn(new InsnNode(opcode));
    }

    @Override
    public void visitThrow() {
        visitConvertedInsn(new InsnNode(Opcodes.ATHROW));
    }

    @Override
    public void visitMonitorEnter() {
        visitConvertedInsn(new InsnNode(Opcodes.MONITORENTER));
    }

    @Override
    public void visitMonitorExit() {
        visitConvertedInsn(new InsnNode(Opcodes.MONITOREXIT));
    }

    @Override
    public void visitFieldGet(FieldRef fieldRef, boolean isStatic) {
        int opcode = isStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
        convertFieldInsnNode(opcode, fieldRef);
    }

    @Override
    public void visitFieldSet(FieldRef fieldRef, boolean isStatic) {
        int opcode = isStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
        convertFieldInsnNode(opcode, fieldRef);
    }

    private void convertFieldInsnNode(int opcode, FieldRef field) {
        String owner = field.getOwner().join('/');
        String name = field.getName();
        String descriptor = AsmUtil.toDescriptor(field.getType());

        visitConvertedInsn(new FieldInsnNode(opcode, owner, name, descriptor));
    }

    @Override
    public void visitInvokeInsn(InvokeInsn.InvokeType invoke, InvokableMethodRef method) {
        int opcode = getInvokeOpcode(invoke);
        String owner = method.getOwner().join('/');
        String name = method.getName();
        String descriptor = AsmUtil.methodDescriptorToString(method.getReturnType(), method.getArguments());
        boolean isInterface = method.isInterface();

        visitConvertedInsn(new MethodInsnNode(opcode, owner, name, descriptor, isInterface));
    }

    private int getInvokeOpcode(InvokeInsn.InvokeType invokeType) {
        switch (invokeType) {
            case VIRTUAL: return Opcodes.INVOKEVIRTUAL;
            case SPECIAL: return Opcodes.INVOKESPECIAL;
            case INTERFACE: return Opcodes.INVOKEINTERFACE;
            case STATIC: return Opcodes.INVOKESTATIC;
            default: throw new AssertionError();
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, MethodDescriptor descriptor, BootstrapMethodHandle bootstrapMethod, List<BootstrapConstant> bootstrapArguments) {
        String asmDescriptor = AsmUtil.methodDescriptorToString(descriptor);
        org.objectweb.asm.Handle asmBootstrapHandle = convertHandle(bootstrapMethod);
        Object[] asmBootstrapArguments = bootstrapArguments.stream()
                .map(this::convertBootstrapConstant)
                .toArray();

        visitConvertedInsn(new InvokeDynamicInsnNode(name, asmDescriptor, asmBootstrapHandle, asmBootstrapArguments));
    }

    private Object convertBootstrapConstant(BootstrapConstant constant) {
        if (constant instanceof IntConstant) {
            return ((IntConstant) constant).getValue();
        } else if (constant instanceof LongConstant) {
            return ((LongConstant) constant).getValue();
        } else if (constant instanceof FloatConstant) {
            return ((FloatConstant) constant).getValue();
        } else if (constant instanceof DoubleConstant) {
            return ((DoubleConstant) constant).getValue();
        } else if (constant instanceof StringConstant) {
            return ((StringConstant) constant).getValue();
        } else if (constant instanceof ClassConstant) {
            RefType refType = ((ClassConstant) constant).getValue();
            return AsmUtil.toAsmType(refType);
        } else if (constant instanceof MethodHandleConstant) {
            Handle handle = ((MethodHandleConstant) constant).getHandle();
            return convertHandle(handle);
        } else if (constant instanceof MethodTypeConstant) {
            MethodTypeConstant methodType = (MethodTypeConstant) constant;
            return AsmUtil.methodDescriptorToType(methodType.getReturnType(), methodType.getArgumentTypes());
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public void visitGoto(Instruction target) {
        convertJumpInsnNode(target, Opcodes.GOTO);
    }

    @Override
    public void visitIf(IfInsn.Condition condition, Instruction target) {
        convertJumpInsnNode(target, getIfOpcode(condition));
    }

    private void convertJumpInsnNode(Instruction target, int aGoto) {
        JumpInsnNode node = new JumpInsnNode(aGoto, null);
        ctx.registerLabel(target, Cell.of(() -> node.label, x -> node.label = x));
        visitConvertedInsn(node);
    }

    private int getIfOpcode(IfInsn.Condition condition) {
        if (condition instanceof IfInsn.IntCondition) {
            IfInsn.IntCondition intCondition = (IfInsn.IntCondition) condition;
            IfInsn.IntCompareValue compareValue = intCondition.getCompareValue();
            IfInsn.IntComparison comparison = intCondition.getComparison();

            if (compareValue instanceof IfInsn.ZeroValue) {
                return comparison instanceof IfInsn.EQ ? Opcodes.IFEQ :
                        comparison instanceof IfInsn.NE ? Opcodes.IFNE :
                        comparison instanceof IfInsn.GE ? Opcodes.IFGE :
                        comparison instanceof IfInsn.GT ? Opcodes.IFGT :
                        comparison instanceof IfInsn.LE ? Opcodes.IFLE :
                        comparison instanceof IfInsn.LT ? Opcodes.IFLT :
                        assertionError();
            } else if (compareValue instanceof IfInsn.StackValue) {
                return comparison instanceof IfInsn.EQ ? Opcodes.IF_ICMPEQ :
                        comparison instanceof IfInsn.NE ? Opcodes.IF_ICMPNE :
                        comparison instanceof IfInsn.GE ? Opcodes.IF_ICMPGE :
                        comparison instanceof IfInsn.GT ? Opcodes.IF_ICMPGT :
                        comparison instanceof IfInsn.LE ? Opcodes.IF_ICMPLE :
                        comparison instanceof IfInsn.LT ? Opcodes.IF_ICMPLT :
                        assertionError();
            }
        } else if (condition instanceof IfInsn.ReferenceCondition) {
            IfInsn.ReferenceCondition refCondition = (IfInsn.ReferenceCondition) condition;
            IfInsn.ReferenceCompareValue compareValue = refCondition.getCompareValue();
            IfInsn.ReferenceComparison comparison = refCondition.getComparison();

            if (compareValue instanceof IfInsn.NullValue) {
                return comparison instanceof IfInsn.EQ ? Opcodes.IFNULL:
                        comparison instanceof IfInsn.NE ? Opcodes.IFNONNULL:
                        assertionError();
            } else if (compareValue instanceof IfInsn.StackValue) {
                return comparison instanceof IfInsn.EQ ? Opcodes.IF_ACMPEQ:
                        comparison instanceof IfInsn.NE ? Opcodes.IF_ACMPNE:
                        assertionError();
            }
        }

        throw new AssertionError();
    }

    @Override
    public void visitSwitch(Map<Integer, Instruction> targetTable, Instruction defaultTarget) {
        TreeSet<Integer> keySet = new TreeSet<>(targetTable.keySet());
        if (isTableSwitch(keySet)) {
            int min = keySet.first();
            int max = keySet.last();
            TableSwitchInsnNode node = new TableSwitchInsnNode(min, max, null, new LabelNode[max - min + 1]);

            ctx.registerLabel(defaultTarget, Cell.of(() -> node.dflt, x -> node.dflt = x));
            IntStream.rangeClosed(min, max).forEach(key -> {
                Instruction target = targetTable.get(key);
                ctx.registerLabel(target, Cell.of(() -> node.labels.get(key), x -> node.labels.set(key, x)));
            });

            visitConvertedInsn(node);
        } else {
            int[] keys = targetTable.keySet().stream().mapToInt(Integer::intValue).toArray();
            LabelNode[] labels = new LabelNode[targetTable.size()];
            LookupSwitchInsnNode node = new LookupSwitchInsnNode(null, keys, labels);

            ctx.registerLabel(defaultTarget, Cell.of(() -> node.dflt, x -> node.dflt = x));
            targetTable.forEach((key, value) -> {
                int index = node.keys.indexOf(key);
                ctx.registerLabel(value, Cell.of(() -> node.labels.get(index), x -> node.labels.set(index, x)));
            });

            visitConvertedInsn(node);
        }
    }

    private boolean isTableSwitch(TreeSet<Integer> keys) {
        Integer prevKey = null;

        for (Integer key : keys) {
            if (prevKey != null && key != prevKey + 1) {
                return false;
            }
            prevKey = key;
        }

        return true;
    }
}
