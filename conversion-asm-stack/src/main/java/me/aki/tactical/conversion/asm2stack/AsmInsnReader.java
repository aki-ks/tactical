package me.aki.tactical.conversion.asm2stack;

import me.aki.tactical.conversion.stackasm.InsnVisitor;
import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.handle.BootstrapMethodHandle;
import me.aki.tactical.core.handle.GetFieldHandle;
import me.aki.tactical.core.handle.GetStaticHandle;
import me.aki.tactical.core.handle.Handle;
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
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.ShortType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.InvokableMethodRef;
import me.aki.tactical.stack.Local;
import me.aki.tactical.stack.insn.IfInsn;
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
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility that calls events of {@link InsnVisitor} based on asm {@link AbstractInsnNode}.
 *
 * This visitor abstracts away the existence of computational types 2.
 *
 * Example:
 * A <tt>DUP2</tt> instruction is used by the jvm to duplicate one <tt>long</tt> value.
 * In such a case, this utility visits a <tt>DUP</tt> insn instead.
 */
public class AsmInsnReader {
    private final InsnVisitor.Asm iv;
    private final ConversionContext ctx;

    public AsmInsnReader(InsnVisitor.Asm iv, ConversionContext ctx) {
        this.iv = iv;
        this.ctx = ctx;
    }

    private Local getLocal(int var) {
        return ctx.getLocal(var);
    }

    public void accept(AbstractInsnNode insn, Frame<BasicValue> frame) {
        switch (insn.getType()) {
            case AbstractInsnNode.INSN:
                convertInsnNode((InsnNode) insn, frame);
                break;

            case AbstractInsnNode.INT_INSN:
                convertIntInsnNode((IntInsnNode) insn);
                break;

            case AbstractInsnNode.VAR_INSN:
                convertVarInsnNode((VarInsnNode) insn);
                break;

            case AbstractInsnNode.TYPE_INSN:
                convertTypeInsnNode((TypeInsnNode) insn);
                break;

            case AbstractInsnNode.FIELD_INSN:
                convertFieldInsnNode((FieldInsnNode) insn);
                break;

            case AbstractInsnNode.METHOD_INSN:
                convertMethodInsnNode((MethodInsnNode) insn);
                break;

            case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                convertInvokeDynamicInsnNode((InvokeDynamicInsnNode) insn);
                break;

            case AbstractInsnNode.JUMP_INSN:
                convertJumpInsnNode((JumpInsnNode) insn);
                break;

            case AbstractInsnNode.LDC_INSN:
                convertLdcInsnNode((LdcInsnNode) insn);
                break;

            case AbstractInsnNode.IINC_INSN:
                convertIIncInsnNode((IincInsnNode) insn);
                break;

            case AbstractInsnNode.TABLESWITCH_INSN:
                convertTableSwitchInsnNode((TableSwitchInsnNode) insn);
                break;

            case AbstractInsnNode.LOOKUPSWITCH_INSN:
                convertLookupSwitchInsnNode((LookupSwitchInsnNode) insn);
                break;

            case AbstractInsnNode.MULTIANEWARRAY_INSN:
                convertMultiANewArrayInsnNode((MultiANewArrayInsnNode) insn);
                break;

            case AbstractInsnNode.LABEL:
            case AbstractInsnNode.FRAME:
            case AbstractInsnNode.LINE:
                break;

            default:
                throw new AssertionError();
        }
    }

    private void convertInsnNode(InsnNode insn, Frame<BasicValue> frame) {
        int opcode = insn.getOpcode();
        switch (opcode) {
            case Opcodes.NOP:
                break;

            case Opcodes.ACONST_NULL:
                iv.visitPush(NullConstant.getInstance());
                break;

            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
                iv.visitPush(new IntConstant(insn.getOpcode() - Opcodes.ICONST_0));
                break;

            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
                iv.visitPush(new LongConstant(insn.getOpcode() - Opcodes.LCONST_0));
                break;

            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
                iv.visitPush(new FloatConstant(insn.getOpcode() - Opcodes.FCONST_0));
                break;

            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
                iv.visitPush(new DoubleConstant(insn.getOpcode() - Opcodes.DCONST_0));
                break;

            case Opcodes.IALOAD:
            case Opcodes.LALOAD:
            case Opcodes.FALOAD:
            case Opcodes.DALOAD:
            case Opcodes.AALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
                iv.visitArrayLoad(getArrayLoadType(opcode));
                break;

            case Opcodes.IASTORE:
            case Opcodes.LASTORE:
            case Opcodes.FASTORE:
            case Opcodes.DASTORE:
            case Opcodes.AASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
                iv.visitArrayStore(getArrayStoreType(opcode));
                break;

            case Opcodes.POP:
            case Opcodes.POP2:
            case Opcodes.DUP:
            case Opcodes.DUP_X1:
            case Opcodes.DUP_X2:
            case Opcodes.DUP2:
            case Opcodes.DUP2_X1:
            case Opcodes.DUP2_X2:
            case Opcodes.SWAP:
                visitStackInsns(insn, frame);
                break;

            case Opcodes.IADD:
            case Opcodes.LADD:
            case Opcodes.FADD:
            case Opcodes.DADD:
                iv.visitAdd(getMathType(opcode));
                break;

            case Opcodes.ISUB:
            case Opcodes.LSUB:
            case Opcodes.FSUB:
            case Opcodes.DSUB:
                iv.visitSub(getMathType(opcode));
                break;

            case Opcodes.IMUL:
            case Opcodes.LMUL:
            case Opcodes.FMUL:
            case Opcodes.DMUL:
                iv.visitMul(getMathType(opcode));
                break;

            case Opcodes.IDIV:
            case Opcodes.LDIV:
            case Opcodes.FDIV:
            case Opcodes.DDIV:
                iv.visitDiv(getMathType(opcode));
                break;

            case Opcodes.IREM:
            case Opcodes.LREM:
            case Opcodes.FREM:
            case Opcodes.DREM:
                iv.visitMod(getMathType(opcode));
                break;

            case Opcodes.INEG:
            case Opcodes.LNEG:
            case Opcodes.FNEG:
            case Opcodes.DNEG:
                iv.visitNeg(getMathType(opcode));
                break;

            case Opcodes.ISHL:
            case Opcodes.LSHL:
                iv.visitShl(getMathType(opcode));
                break;

            case Opcodes.ISHR:
            case Opcodes.LSHR:
                iv.visitShr(getMathType(opcode));
                break;

            case Opcodes.IUSHR:
            case Opcodes.LUSHR:
                iv.visitUShr(getMathType(opcode));
                break;

            case Opcodes.IAND:
            case Opcodes.LAND:
                iv.visitAnd(getMathType(opcode));
                break;

            case Opcodes.IOR:
            case Opcodes.LOR:
                iv.visitOr(getMathType(opcode));
                break;

            case Opcodes.IXOR:
            case Opcodes.LXOR:
                iv.visitXor(getMathType(opcode));
                break;

            case Opcodes.LCMP:
                iv.visitCmp();
                break;

            case Opcodes.FCMPL:
            case Opcodes.DCMPL:
                iv.visitCmpl(getMathType(opcode));
                break;

            case Opcodes.FCMPG:
            case Opcodes.DCMPG:
                iv.visitCmpg(getMathType(opcode));
                break;

            case Opcodes.I2L:
            case Opcodes.I2F:
            case Opcodes.I2D:
            case Opcodes.L2I:
            case Opcodes.L2F:
            case Opcodes.L2D:
            case Opcodes.F2I:
            case Opcodes.F2L:
            case Opcodes.F2D:
            case Opcodes.D2I:
            case Opcodes.D2L:
            case Opcodes.D2F:
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
                iv.visitPrimitiveCast(getCastFromType(opcode), getCastToType(opcode));
                break;

            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.FRETURN:
            case Opcodes.DRETURN:
            case Opcodes.ARETURN:
            case Opcodes.RETURN:
                iv.visitReturn(getReturnType(opcode));
                break;

            case Opcodes.ARRAYLENGTH:
                iv.visitArrayLength();
                break;

            case Opcodes.ATHROW:
                iv.visitThrow();
                break;

            case Opcodes.MONITORENTER:
                iv.visitMonitorEnter();
                break;

            case Opcodes.MONITOREXIT:
                iv.visitMonitorExit();
                break;
        }
    }

    private void visitStackInsns(InsnNode insn, Frame<BasicValue> frame) {
        switch (insn.getOpcode()) {
            case Opcodes.SWAP:
                if (is32bit(frame, 0)) {
                    iv.visitSwap();
                    break;
                }
                throw new StackTypeException();

            case Opcodes.POP:
                if (is32bit(frame, 0)) {
                    iv.visitPop();
                    break;
                }
                throw new StackTypeException();

            case Opcodes.POP2:
                if (is32bit(frame, 0)) {
                    // Form 1
                    iv.visitPop();
                    iv.visitPop();
                    break;
                } else {
                    // Form 2
                    iv.visitPop();
                    break;
                }

            case Opcodes.DUP:
                if (is32bit(frame, 0)) {
                    iv.visitDup();
                    break;
                }
                throw new StackTypeException();

            case Opcodes.DUP_X1:
                if (is32bit(frame, 0) && is32bit(frame, 1)) {
                    iv.visitDupX1();
                    break;
                }
                throw new StackTypeException();

            case Opcodes.DUP_X2:
                if (is32bit(frame, 0)) {
                    if (is32bit(frame, 1)) {
                        if (is32bit(frame, 2)) {
                            // Form 1
                            iv.visitDupX2();
                            break;
                        }
                    } else {
                        // Form 2
                        iv.visitDupX1();
                        break;
                    }
                }
                throw new StackTypeException();

            case Opcodes.DUP2:
                if (is32bit(frame, 0)) {
                    if (is32bit(frame, 1)) {
                        // Form 1
                        iv.visitDup2();
                        break;
                    }
                } else {
                    // Form 2
                    iv.visitDup();
                    break;
                }
                throw new StackTypeException();

            case Opcodes.DUP2_X1:
                if (is32bit(frame, 0)) {
                    if (is32bit(frame, 1)) {
                        if (is32bit(frame, 2)) {
                            // Form 1
                            iv.visitDup2X1();
                            break;
                        }
                    }
                } else {
                    if (is32bit(frame, 1)) {
                        // Form 2
                        iv.visitDupX1();
                        break;
                    }
                }
                throw new StackTypeException();

            case Opcodes.DUP2_X2:
                if (is32bit(frame, 0)) {
                    if (is32bit(frame, 1)) {
                        if (is32bit(frame, 2)) {
                            if (is32bit(frame, 3)) {
                                // Form 1
                                iv.visitDup2X2();
                                break;
                            }
                        } else {
                            // Form 3
                            iv.visitDup2X1();
                            break;
                        }
                    }
                } else {
                    if (is32bit(frame, 1)) {
                        if (is32bit(frame, 2)) {
                            // Form 2
                            iv.visitDupX2();
                            break;
                        }
                    } else {
                        // Form 4
                        iv.visitDupX1();
                        break;
                    }
                }
                throw new StackTypeException();

            default:
                throw new AssertionError();
        }
    }

    /**
     * Is a value n-elements down in the stack of computational type 1.
     *
     * @param frame frame of the current instruction
     * @param index index of stack where 0 is the must recent value
     * @return is the value of computational type 1
     */
    private boolean is32bit(Frame<BasicValue> frame, int index) {
        BasicValue value = frame.getStack(frame.getStackSize() - 1 - index);
        int sort = value.getType().getSort();

        return sort == org.objectweb.asm.Type.LONG ||
                sort == org.objectweb.asm.Type.DOUBLE;
    }

    private Type getArrayLoadType(int opcode) {
        switch (opcode) {
            case Opcodes.IALOAD: return IntType.getInstance();
            case Opcodes.LALOAD: return LongType.getInstance();
            case Opcodes.FALOAD: return FloatType.getInstance();
            case Opcodes.DALOAD: return DoubleType.getInstance();
            case Opcodes.AALOAD: return ObjectType.OBJECT;
            case Opcodes.BALOAD: return ByteType.getInstance();
            case Opcodes.CALOAD: return CharType.getInstance();
            case Opcodes.SALOAD: return ShortType.getInstance();
            default: throw new AssertionError();
        }
    }

    private Type getArrayStoreType(int opcode) {
        switch (opcode) {
            case Opcodes.IASTORE: return IntType.getInstance();
            case Opcodes.LASTORE: return LongType.getInstance();
            case Opcodes.FASTORE: return FloatType.getInstance();
            case Opcodes.DASTORE: return DoubleType.getInstance();
            case Opcodes.AASTORE: return ObjectType.OBJECT;
            case Opcodes.BASTORE: return ByteType.getInstance();
            case Opcodes.CASTORE: return CharType.getInstance();
            case Opcodes.SASTORE: return ShortType.getInstance();
            default: throw new AssertionError();
        }
    }

    private Type getMathType(int opcode) {
        switch (opcode) {
            case Opcodes.DADD:
            case Opcodes.DSUB:
            case Opcodes.DMUL:
            case Opcodes.DDIV:
            case Opcodes.DREM:
            case Opcodes.DNEG:
            case Opcodes.DCMPL:
            case Opcodes.DCMPG:
                return DoubleType.getInstance();

            case Opcodes.FADD:
            case Opcodes.FSUB:
            case Opcodes.FMUL:
            case Opcodes.FDIV:
            case Opcodes.FREM:
            case Opcodes.FNEG:
            case Opcodes.FCMPL:
            case Opcodes.FCMPG:
                return FloatType.getInstance();

            case Opcodes.IADD:
            case Opcodes.ISUB:
            case Opcodes.IMUL:
            case Opcodes.IDIV:
            case Opcodes.IREM:
            case Opcodes.INEG:
            case Opcodes.ISHL:
            case Opcodes.ISHR:
            case Opcodes.IUSHR:
            case Opcodes.IAND:
            case Opcodes.IOR:
            case Opcodes.IXOR:
                return IntType.getInstance();

            case Opcodes.LADD:
            case Opcodes.LSUB:
            case Opcodes.LMUL:
            case Opcodes.LDIV:
            case Opcodes.LREM:
            case Opcodes.LNEG:
            case Opcodes.LSHL:
            case Opcodes.LSHR:
            case Opcodes.LUSHR:
            case Opcodes.LAND:
            case Opcodes.LOR:
            case Opcodes.LXOR:
                return LongType.getInstance();

            default:
                throw new AssertionError();
        }
    }

    private PrimitiveType getCastFromType(int opcode) {
        switch (opcode) {
            case Opcodes.I2L:
            case Opcodes.I2F:
            case Opcodes.I2D:
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
                return IntType.getInstance();

            case Opcodes.L2I:
            case Opcodes.L2F:
            case Opcodes.L2D:
                return LongType.getInstance();

            case Opcodes.F2I:
            case Opcodes.F2L:
            case Opcodes.F2D:
                return FloatType.getInstance();

            case Opcodes.D2I:
            case Opcodes.D2L:
            case Opcodes.D2F:
                return DoubleType.getInstance();

            default:
                throw new AssertionError();
        }
    }

    private PrimitiveType getCastToType(int opcode) {
        switch (opcode) {
            case Opcodes.I2B: return ByteType.getInstance();
            case Opcodes.I2C: return CharType.getInstance();
            case Opcodes.I2S: return ShortType.getInstance();

            case Opcodes.I2D:
            case Opcodes.L2D:
            case Opcodes.F2D:
                return DoubleType.getInstance();

            case Opcodes.I2F:
            case Opcodes.L2F:
            case Opcodes.D2F:
                return FloatType.getInstance();

            case Opcodes.L2I:
            case Opcodes.F2I:
            case Opcodes.D2I:
                return IntType.getInstance();

            case Opcodes.I2L:
            case Opcodes.F2L:
            case Opcodes.D2L:
                return LongType.getInstance();

            default:
                throw new AssertionError();
        }
    }

    private Optional<Type> getReturnType(int opcode) {
        switch (opcode) {
            case Opcodes.IRETURN: return Optional.of(IntType.getInstance());
            case Opcodes.LRETURN: return Optional.of(LongType.getInstance());
            case Opcodes.FRETURN: return Optional.of(FloatType.getInstance());
            case Opcodes.DRETURN: return Optional.of(DoubleType.getInstance());
            case Opcodes.ARETURN: return Optional.of(ObjectType.OBJECT);
            case Opcodes.RETURN: return Optional.empty();
            default: throw new AssertionError();
        }
    }

    private void convertIntInsnNode(IntInsnNode insn) {
        switch (insn.getOpcode()) {
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                iv.visitPush(new IntConstant(insn.operand));
                break;

            case Opcodes.NEWARRAY:
                ArrayType array = new ArrayType(getArrayBaseType(insn.operand), 1);
                iv.visitNewArray(array, 1);
                break;

            default:
                throw new AssertionError();
        }
    }

    private Type getArrayBaseType(int operand) {
        switch (operand) {
            case Opcodes.T_BOOLEAN: return BooleanType.getInstance();
            case Opcodes.T_CHAR: return CharType.getInstance();
            case Opcodes.T_FLOAT: return FloatType.getInstance();
            case Opcodes.T_DOUBLE: return DoubleType.getInstance();
            case Opcodes.T_BYTE: return ByteType.getInstance();
            case Opcodes.T_SHORT: return ShortType.getInstance();
            case Opcodes.T_INT: return IntType.getInstance();
            case Opcodes.T_LONG: return LongType.getInstance();
            default: throw new AssertionError();
        }
    }

    private void convertVarInsnNode(VarInsnNode insn) {
        int opcode = insn.getOpcode();
        switch (opcode) {
            case Opcodes.ILOAD:
            case Opcodes.LLOAD:
            case Opcodes.FLOAD:
            case Opcodes.DLOAD:
            case Opcodes.ALOAD:
                iv.visitLoad(getVarLoadStoreType(opcode), getLocal(insn.var));
                break;

            case Opcodes.ISTORE:
            case Opcodes.LSTORE:
            case Opcodes.FSTORE:
            case Opcodes.DSTORE:
            case Opcodes.ASTORE:
                iv.visitStore(getVarLoadStoreType(opcode), getLocal(insn.var));
                break;

            case Opcodes.RET:
                throw new IllegalStateException("Subroutine was not inlined by JSRInlinerAdapter");

            default:
                throw new AssertionError();
        }
    }

    private Type getVarLoadStoreType(int opcode) {
        switch (opcode) {
            case Opcodes.ILOAD:
            case Opcodes.ISTORE:
                return IntType.getInstance();

            case Opcodes.LLOAD:
            case Opcodes.LSTORE:
                return LongType.getInstance();

            case Opcodes.FLOAD:
            case Opcodes.FSTORE:
                return FloatType.getInstance();

            case Opcodes.DLOAD:
            case Opcodes.DSTORE:
                return DoubleType.getInstance();

            case Opcodes.ALOAD:
            case Opcodes.ASTORE:
                return ObjectType.OBJECT;

            default:
                throw new AssertionError();
        }
    }

    private void convertTypeInsnNode(TypeInsnNode insn) {
        switch (insn.getOpcode()) {
            case Opcodes.NEW:
                iv.visitNew(AsmUtil.pathFromInternalName(insn.desc));
                break;

            case Opcodes.ANEWARRAY:
                RefType baseType = AsmUtil.refTypeFromInternalName(insn.desc);
                iv.visitNewArray(new ArrayType(baseType, 1), 1);
                break;

            case Opcodes.CHECKCAST:
                iv.visitReferenceCast(AsmUtil.refTypeFromInternalName(insn.desc));
                break;

            case Opcodes.INSTANCEOF:
                iv.visitInstanceOf(AsmUtil.refTypeFromInternalName(insn.desc));
                break;

            default:
                throw new AssertionError();
        }
    }

    private void convertFieldInsnNode(FieldInsnNode insn) {
        Path fieldOwner = AsmUtil.pathFromInternalName(insn.owner);
        Type fieldType = AsmUtil.fromDescriptor(insn.desc);
        FieldRef fieldRef = new FieldRef(fieldOwner, insn.name, fieldType);

        switch (insn.getOpcode()) {
            case Opcodes.GETFIELD:
                iv.visitFieldGet(fieldRef, false);
                break;

            case Opcodes.GETSTATIC:
                iv.visitFieldGet(fieldRef, true);
                break;

            case Opcodes.PUTFIELD:
                iv.visitFieldSet(fieldRef, false);
                break;

            case Opcodes.PUTSTATIC:
                iv.visitFieldSet(fieldRef, true);
                break;

            default:
                throw new AssertionError();
        }
    }

    private void convertMethodInsnNode(MethodInsnNode insn) {
        Path owner = AsmUtil.pathFromInternalName(insn.owner);
        MethodDescriptor desc = AsmUtil.parseMethodDescriptor(insn.desc);
        InvokableMethodRef methodRef = new InvokableMethodRef(owner, insn.name, desc.getParameterTypes(), desc.getReturnType(), insn.itf);

        iv.visitInvokeInsn(getInvokeType(insn.getOpcode()), methodRef);
    }

    private InvokeInsn.InvokeType getInvokeType(int opcode) {
        switch (opcode) {
            case Opcodes.INVOKEVIRTUAL: return InvokeInsn.InvokeType.VIRTUAL;
            case Opcodes.INVOKESPECIAL: return InvokeInsn.InvokeType.SPECIAL;
            case Opcodes.INVOKESTATIC: return InvokeInsn.InvokeType.STATIC;
            case Opcodes.INVOKEINTERFACE: return InvokeInsn.InvokeType.INTERFACE;
            default: throw new AssertionError();
        }
    }

    private void convertInvokeDynamicInsnNode(InvokeDynamicInsnNode insn) {
        MethodDescriptor descriptor = AsmUtil.parseMethodDescriptor(insn.desc);
        BootstrapMethodHandle bootstrapMethod = (BootstrapMethodHandle) convertMethodHandle(insn.bsm);
        List<BootstrapConstant> bootstrapArguments = Arrays.stream(insn.bsmArgs)
                .map(this::convertBootstrapArgument)
                .collect(Collectors.toList());

        iv.visitInvokeDynamicInsn(insn.name, descriptor, bootstrapMethod, bootstrapArguments);
    }

    private BootstrapConstant convertBootstrapArgument(Object value) {
        if (value instanceof Integer) {
            return new IntConstant((Integer) value);
        } else if (value instanceof Long) {
            return new LongConstant((Long) value);
        } else if (value instanceof Float) {
            return new FloatConstant((Float) value);
        } else if (value instanceof Double) {
            return new DoubleConstant((Double) value);
        } else if (value instanceof String) {
            return new StringConstant((String) value);
        } else if (value instanceof org.objectweb.asm.Type) {
            org.objectweb.asm.Type asmType = (org.objectweb.asm.Type) value;
            switch (asmType.getSort()) {
                case org.objectweb.asm.Type.METHOD:
                    MethodDescriptor desc = AsmUtil.parseMethodDescriptor(asmType);
                    return new MethodTypeConstant(desc.getParameterTypes(), desc.getReturnType());

                case org.objectweb.asm.Type.OBJECT:
                case org.objectweb.asm.Type.ARRAY:
                    return new ClassConstant((RefType) AsmUtil.fromAsmType(asmType));

                default:
                    throw new AssertionError();
            }
        } else if (value instanceof org.objectweb.asm.Handle) {
            return new MethodHandleConstant(convertMethodHandle((org.objectweb.asm.Handle) value));
        } else {
            throw new AssertionError();
        }
    }

    private void convertJumpInsnNode(JumpInsnNode insn) {
        int opcode = insn.getOpcode();
        switch (opcode) {
            case Opcodes.GOTO:
                iv.visitGoto(insn.label);
                return;

            case Opcodes.JSR:
                throw new IllegalStateException("Subroutine was not inlined by JSRInlinerAdapter");

            default:
                IfInsn.Condition condition = getIfCondition(opcode);
                iv.visitIf(condition, insn.label);
        }
    }

    private IfInsn.Condition getIfCondition(int opcode) {
        switch (opcode) {
            case Opcodes.IFNULL: return IfInsn.IF_NULL;
            case Opcodes.IFNONNULL: return IfInsn.IF_NONNULL;

            case Opcodes.IFEQ: return IfInsn.IF_EQ_ZERO;
            case Opcodes.IFNE: return IfInsn.IF_NE_ZERO;
            case Opcodes.IFLT: return IfInsn.IF_LT_ZERO;
            case Opcodes.IFGE: return IfInsn.IF_GE_ZERO;
            case Opcodes.IFGT: return IfInsn.IF_GT_ZERO;
            case Opcodes.IFLE: return IfInsn.IF_LE_ZERO;

            case Opcodes.IF_ACMPEQ: return IfInsn.IF_REF_EQ;
            case Opcodes.IF_ACMPNE: return IfInsn.IF_REF_NE;

            case Opcodes.IF_ICMPEQ: return IfInsn.IF_INT_EQ;
            case Opcodes.IF_ICMPNE: return IfInsn.IF_INT_NE;
            case Opcodes.IF_ICMPLT: return IfInsn.IF_INT_LT;
            case Opcodes.IF_ICMPGE: return IfInsn.IF_INT_GE;
            case Opcodes.IF_ICMPGT: return IfInsn.IF_INT_GT;
            case Opcodes.IF_ICMPLE: return IfInsn.IF_INT_LE;

            default: throw new AssertionError();
        }
    }

    private void convertLdcInsnNode(LdcInsnNode insn) {
        iv.visitPush(convertLdcValue(insn.cst));
    }

    private Constant convertLdcValue(Object value) {
        if (value instanceof Integer) {
            return new IntConstant((Integer) value);
        } else if (value instanceof Long) {
            return new LongConstant((Long) value);
        } else if (value instanceof Float) {
            return new FloatConstant((Float) value);
        } else if (value instanceof Double) {
            return new DoubleConstant((Double) value);
        } else if (value instanceof String) {
            return new StringConstant((String) value);
        } else if (value instanceof org.objectweb.asm.Type) {
            org.objectweb.asm.Type asmType = (org.objectweb.asm.Type) value;
            switch (asmType.getSort()) {
                case org.objectweb.asm.Type.METHOD:
                    MethodDescriptor desc = AsmUtil.parseMethodDescriptor(asmType);
                    return new MethodTypeConstant(desc.getParameterTypes(), desc.getReturnType());

                case org.objectweb.asm.Type.OBJECT:
                case org.objectweb.asm.Type.ARRAY:
                    return new ClassConstant((RefType) AsmUtil.fromAsmType(asmType));

                default:
                    throw new AssertionError();
            }
        } else if (value instanceof org.objectweb.asm.Handle) {
            return new MethodHandleConstant(convertMethodHandle((org.objectweb.asm.Handle) value));
        } else {
            throw new AssertionError();
        }
    }

    private Handle convertMethodHandle(org.objectweb.asm.Handle handle) {
        Path owner = AsmUtil.pathFromInternalName(handle.getOwner());
        int tag = handle.getTag();
        switch (tag) {
            case Opcodes.H_GETFIELD:
            case Opcodes.H_GETSTATIC:
            case Opcodes.H_PUTFIELD:
            case Opcodes.H_PUTSTATIC:
                FieldRef fieldRef = new FieldRef(owner, handle.getName(), AsmUtil.fromDescriptor(handle.getDesc()));

                switch (tag) {
                    case Opcodes.H_GETFIELD: return new GetFieldHandle(fieldRef);
                    case Opcodes.H_GETSTATIC: return new GetStaticHandle(fieldRef);
                    case Opcodes.H_PUTFIELD: return new SetFieldHandle(fieldRef);
                    case Opcodes.H_PUTSTATIC: return new SetStaticHandle(fieldRef);
                    default: throw new AssertionError();
                }

            case Opcodes.H_INVOKEVIRTUAL:
            case Opcodes.H_INVOKESTATIC:
            case Opcodes.H_INVOKESPECIAL:
            case Opcodes.H_NEWINVOKESPECIAL:
            case Opcodes.H_INVOKEINTERFACE:
                MethodDescriptor desc = AsmUtil.parseMethodDescriptor(handle.getDesc());
                MethodRef methodRef = new MethodRef(owner, handle.getName(), desc.getParameterTypes(), desc.getReturnType());

                switch (tag) {
                    case Opcodes.H_INVOKEVIRTUAL: return new InvokeVirtualHandle(methodRef);
                    case Opcodes.H_INVOKESTATIC: return new InvokeStaticHandle(methodRef, handle.isInterface());
                    case Opcodes.H_INVOKESPECIAL: return new InvokeSpecialHandle(methodRef, handle.isInterface());
                    case Opcodes.H_NEWINVOKESPECIAL: return new NewInstanceHandle(methodRef);
                    case Opcodes.H_INVOKEINTERFACE: return new InvokeInterfaceHandle(methodRef);
                    default: throw new AssertionError();
                }

            default:
                throw new AssertionError();
        }
    }

    private void convertIIncInsnNode(IincInsnNode insn) {
        iv.visitIncrement(getLocal(insn.var), insn.incr);
    }

    private void convertTableSwitchInsnNode(TableSwitchInsnNode insn) {
        Map<Integer, LabelNode> table = new HashMap<>();
        Iterator<LabelNode> labelIter = insn.labels.iterator();
        for (int key = insn.min; key <= insn.max; key++) {
            table.put(key, labelIter.next());
        }

        iv.visitSwitch(table, insn.dflt);
    }

    private void convertLookupSwitchInsnNode(LookupSwitchInsnNode insn) {
        Map<Integer, LabelNode> table = new HashMap<>();
        Iterator<LabelNode> labelIter = insn.labels.iterator();
        for (Integer key : insn.keys) {
            table.put(key, labelIter.next());
        }

        iv.visitSwitch(table, insn.dflt);
    }

    private void convertMultiANewArrayInsnNode(MultiANewArrayInsnNode insn) {
        ArrayType array = (ArrayType) AsmUtil.fromDescriptor(insn.desc);
        iv.visitNewArray(array, insn.dims);
    }

    public static class StackTypeException extends IllegalStateException {
        public StackTypeException() {}

        public StackTypeException(String s) {
            super(s);
        }
    }
}
