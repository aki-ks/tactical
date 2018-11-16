package me.aki.tactical.conversion.ref2stack;

import me.aki.tactical.conversion.stackasm.StackInsnVisitor;
import me.aki.tactical.core.constant.IntConstant;
import me.aki.tactical.core.constant.NullConstant;
import me.aki.tactical.core.type.IntLikeType;
import me.aki.tactical.core.type.IntType;
import me.aki.tactical.core.type.LongType;
import me.aki.tactical.core.type.PrimitiveType;
import me.aki.tactical.core.type.RefType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.Variable;
import me.aki.tactical.ref.condition.Condition;
import me.aki.tactical.ref.condition.Equal;
import me.aki.tactical.ref.condition.GreaterEqual;
import me.aki.tactical.ref.condition.GreaterThan;
import me.aki.tactical.ref.condition.LessEqual;
import me.aki.tactical.ref.condition.LessThan;
import me.aki.tactical.ref.condition.NonEqual;
import me.aki.tactical.ref.expr.AbstractBinaryExpr;
import me.aki.tactical.ref.expr.AbstractFieldExpr;
import me.aki.tactical.ref.expr.AddExpr;
import me.aki.tactical.ref.expr.AndExpr;
import me.aki.tactical.ref.expr.ArrayBoxExpr;
import me.aki.tactical.ref.expr.ArrayLengthExpr;
import me.aki.tactical.ref.expr.CastExpr;
import me.aki.tactical.ref.expr.CmpExpr;
import me.aki.tactical.ref.expr.CmpgExpr;
import me.aki.tactical.ref.expr.CmplExpr;
import me.aki.tactical.ref.expr.ConstantExpr;
import me.aki.tactical.ref.expr.DivExpr;
import me.aki.tactical.ref.expr.InstanceFieldExpr;
import me.aki.tactical.ref.expr.InstanceOfExpr;
import me.aki.tactical.ref.expr.InvokeExpr;
import me.aki.tactical.ref.expr.ModExpr;
import me.aki.tactical.ref.expr.MulExpr;
import me.aki.tactical.ref.expr.NegExpr;
import me.aki.tactical.ref.expr.NewArrayExpr;
import me.aki.tactical.ref.expr.NewExpr;
import me.aki.tactical.ref.expr.OrExpr;
import me.aki.tactical.ref.expr.ShlExpr;
import me.aki.tactical.ref.expr.ShrExpr;
import me.aki.tactical.ref.expr.StaticFieldExpr;
import me.aki.tactical.ref.expr.SubExpr;
import me.aki.tactical.ref.expr.UShrExpr;
import me.aki.tactical.ref.expr.XorExpr;
import me.aki.tactical.ref.invoke.AbstractInstanceInvoke;
import me.aki.tactical.ref.invoke.AbstractInvoke;
import me.aki.tactical.ref.invoke.InvokeDynamic;
import me.aki.tactical.ref.invoke.InvokeInterface;
import me.aki.tactical.ref.invoke.InvokeSpecial;
import me.aki.tactical.ref.invoke.InvokeStatic;
import me.aki.tactical.ref.invoke.InvokeVirtual;
import me.aki.tactical.ref.stmt.AssignStmt;
import me.aki.tactical.ref.stmt.BranchStmt;
import me.aki.tactical.ref.stmt.GotoStmt;
import me.aki.tactical.ref.stmt.IfStmt;
import me.aki.tactical.ref.stmt.InvokeStmt;
import me.aki.tactical.ref.stmt.MonitorEnterStmt;
import me.aki.tactical.ref.stmt.MonitorExitStmt;
import me.aki.tactical.ref.stmt.ReturnStmt;
import me.aki.tactical.ref.stmt.SwitchStmt;
import me.aki.tactical.ref.stmt.ThrowStmt;
import me.aki.tactical.stack.StackLocal;
import me.aki.tactical.stack.insn.IfInsn;
import me.aki.tactical.stack.invoke.DynamicInvoke;
import me.aki.tactical.stack.invoke.InterfaceInvoke;
import me.aki.tactical.stack.invoke.Invoke;
import me.aki.tactical.stack.invoke.SpecialInvoke;
import me.aki.tactical.stack.invoke.StaticInvoke;
import me.aki.tactical.stack.invoke.VirtualInvoke;

import java.util.List;
import java.util.Optional;

/**
 * Utility that calls events corresponding to {@link Statement Statements} or
 * {@link Expression Expressions} on a {@link StackInsnVisitor}.
 */
public class RefInsnReader {
    private final ConversionContext ctx;
    private final StackInsnVisitor<Statement> iv;

    private static <T> T assertionError() {
        throw new AssertionError();
    }

    public RefInsnReader(ConversionContext ctx, StackInsnVisitor<Statement> iv) {
        this.ctx = ctx;
        this.iv = iv;
    }

    public void accept(Statement statement) {
        if (statement instanceof BranchStmt) {
            if (statement instanceof GotoStmt) {
                iv.visitGoto(((GotoStmt) statement).getTarget());
            } else if (statement instanceof IfStmt) {
                convertIfStatement((IfStmt) statement);
            } else if (statement instanceof SwitchStmt) {
                iv.visitSwitch(((SwitchStmt) statement).getBranchTable(), ((SwitchStmt) statement).getDefaultTarget());
            } else {
                throw new AssertionError();
            }
        } else if (statement instanceof AssignStmt) {
            convertAssignStatement((AssignStmt) statement);
        } else if (statement instanceof InvokeStmt) {
            convertInvoke(((InvokeStmt) statement).getInvoke());
            // the returned value of the invoke expressions is not used.
            iv.visitPop();
        } else if (statement instanceof ReturnStmt) {
            convertReturnStatement((ReturnStmt) statement);
        } else if (statement instanceof ThrowStmt) {
            ThrowStmt throwStmt = (ThrowStmt) statement;
            accept(throwStmt.getValue());
            iv.visitThrow();
        } else if (statement instanceof MonitorEnterStmt) {
            MonitorEnterStmt monitorEnter = (MonitorEnterStmt) statement;
            accept(monitorEnter.getValue());
            iv.visitMonitorEnter();
        } else if (statement instanceof MonitorExitStmt) {
            MonitorExitStmt monitorExit = (MonitorExitStmt) statement;
            accept(monitorExit.getValue());
            iv.visitMonitorExit();
        } else {
            throw new AssertionError();
        }
    }

    private void convertIfStatement(IfStmt statement) {
        Condition refCondition = statement.getCondition();
        Expression value1 = refCondition.getValue1();
        Expression value2 = refCondition.getValue2();

        accept(value1);

        IfInsn.Condition stackCondition;
        if (value1 instanceof IntLikeType) {
            if (!(value2 instanceof IntLikeType)) {
                throw new IllegalStateException();
            }

            IfInsn.IntComparison comparison =
                    refCondition instanceof Equal ? IfInsn.EQ.getInstance() :
                    refCondition instanceof NonEqual ? IfInsn.NE.getInstance() :
                    refCondition instanceof GreaterEqual ? IfInsn.GE.getInstance() :
                    refCondition instanceof GreaterThan ? IfInsn.GT.getInstance() :
                    refCondition instanceof LessEqual ? IfInsn.LE.getInstance() :
                    refCondition instanceof LessThan ? IfInsn.LT.getInstance() :
                    assertionError();

            boolean isZeroCompare = value2 instanceof ConstantExpr && ((ConstantExpr) value2).getConstant().equals(new IntConstant(0));
            IfInsn.IntCompareValue compareValue = isZeroCompare ? IfInsn.ZeroValue.getInstance() : IfInsn.StackValue.getInstance();

            if (!isZeroCompare) {
                accept(value2);
            }
            stackCondition = new IfInsn.IntCondition(comparison, compareValue);
        } else if (value1 instanceof RefType) {
            if (!(value2 instanceof RefType)) {
                throw new IllegalStateException();
            }

            IfInsn.ReferenceComparison comparison =
                    refCondition instanceof Equal ? IfInsn.EQ.getInstance() :
                    refCondition instanceof NonEqual ? IfInsn.NE.getInstance() :
                    assertionError();

            boolean isNullCompare = value2 instanceof ConstantExpr && ((ConstantExpr) value2).getConstant() == NullConstant.getInstance();
            IfInsn.ReferenceCompareValue compareValue = isNullCompare ? IfInsn.NullValue.getInstance() : IfInsn.StackValue.getInstance();

            if (!isNullCompare) {
                accept(value2);
            }
            stackCondition = new IfInsn.ReferenceCondition(comparison, compareValue);
        } else {
            throw new AssertionError();
        }

        iv.visitIf(stackCondition, statement.getTarget());
    }

    private void convertAssignStatement(AssignStmt statement) {
        Variable variable = statement.getVariable();
        Expression value = statement.getValue();

        if (variable instanceof ArrayBoxExpr) {
            accept(value);
            iv.visitArrayStore(value.getType());
        } else if (variable instanceof RefLocal) {
            accept(value);
            iv.visitStore(value.getType(), ctx.getStackLocal((RefLocal) variable));
        } else if (variable instanceof AbstractFieldExpr) {
            AbstractFieldExpr fieldExpr = (AbstractFieldExpr) variable;
            boolean isStatic = value instanceof StaticFieldExpr;

            if (value instanceof InstanceFieldExpr) {
                accept(((InstanceFieldExpr) value).getInstance());
            }
            accept(value);
            iv.visitFieldSet(fieldExpr.getField(), isStatic);
        } else {
            throw new AssertionError();
        }
    }

    private void convertInvoke(AbstractInvoke abstractInvoke) {
        if (abstractInvoke instanceof AbstractInstanceInvoke) {
            Expression instance = ((AbstractInstanceInvoke) abstractInvoke).getInstance();
            accept(instance);
        }

        for (Expression argument : abstractInvoke.getArguments()) {
            accept(argument);
        }

        Invoke stackInvoke;
        if (abstractInvoke instanceof InvokeDynamic) {
            InvokeDynamic invoke = (InvokeDynamic) abstractInvoke;
            stackInvoke = new DynamicInvoke(invoke.getName(), invoke.getDescriptor(), invoke.getBootstrapMethod(), invoke.getBootstrapArguments());
        } else if (abstractInvoke instanceof InvokeStatic) {
            InvokeStatic invoke = (InvokeStatic) abstractInvoke;
            stackInvoke = new StaticInvoke(invoke.getMethod(), invoke.isInterface());
        } else if (abstractInvoke instanceof InvokeInterface) {
            InvokeInterface invoke = (InvokeInterface) abstractInvoke;
            stackInvoke = new InterfaceInvoke(invoke.getMethod());
        } else if (abstractInvoke instanceof InvokeSpecial) {
            InvokeSpecial invoke = (InvokeSpecial) abstractInvoke;
            stackInvoke = new SpecialInvoke(invoke.getMethod(), invoke.isInterface());
        } else if (abstractInvoke instanceof InvokeVirtual) {
            InvokeVirtual invoke = (InvokeVirtual) abstractInvoke;
            stackInvoke = new VirtualInvoke(invoke.getMethod());
        } else {
            throw new AssertionError();
        }
        iv.visitInvokeInsn(stackInvoke);
    }

    private void convertReturnStatement(ReturnStmt statement) {
        statement.getValue().ifPresentOrElse(value -> {
            accept(value);
            iv.visitReturn(Optional.of(value.getType()));
        }, () -> {
            iv.visitReturn(Optional.empty());
        });
    }

    public void accept(Expression expression) {
        if (expression instanceof AbstractBinaryExpr) {
            convertBinaryExpression(expression);
        } else if (expression instanceof Variable) {
            convertVariable(expression);
        } else if (expression instanceof ConstantExpr) {
            iv.visitPush(((ConstantExpr) expression).getConstant());
        } else if (expression instanceof InvokeExpr) {
            convertInvoke(((InvokeExpr) expression).getInvoke());
        } else if (expression instanceof CastExpr) {
            convertCast((CastExpr) expression);
        } else if (expression instanceof InstanceOfExpr) {
            InstanceOfExpr expr = (InstanceOfExpr) expression;
            accept(expr.getValue());
            iv.visitInstanceOf(expr.getCheckType());
        } else if (expression instanceof ArrayLengthExpr) {
            ArrayLengthExpr expr = (ArrayLengthExpr) expression;
            accept(expr.getArray());
            iv.visitArrayLength();
        } else if (expression instanceof NewArrayExpr) {
            convertNewArrayExpr((NewArrayExpr) expression);
        } else if (expression instanceof NewExpr) {
            iv.visitNew(((NewExpr) expression).getPath());
        } else if (expression instanceof NegExpr) {
            NegExpr expr = (NegExpr) expression;
            accept(expr.getValue());
            iv.visitNeg(expr.getType());
        } else {
            throw new AssertionError();
        }
    }

    private void convertBinaryExpression(Expression expression) {
        AbstractBinaryExpr binaryExpr = (AbstractBinaryExpr) expression;
        Expression value1 = binaryExpr.getValue1();
        Expression value2 = binaryExpr.getValue2();
        Type type1 = value1.getType();
        Type type2 = value2.getType();

        accept(value1);
        accept(value2);

        if (expression instanceof AddExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitAdd(type1);
        } else if (expression instanceof SubExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitSub(type1);
        } else if (expression instanceof MulExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitMul(type1);
        } else if (expression instanceof DivExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitDiv(type1);
        } else if (expression instanceof ModExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitMod(type1);
        } else if (expression instanceof AndExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitAnd(type1);
        } else if (expression instanceof OrExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitOr(type1);
        } else if (expression instanceof XorExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitXor(type1);
        } else if (expression instanceof CmpExpr) {
            requireEqualTypes(LongType.getInstance(), type1);
            requireEqualTypes(LongType.getInstance(), type2);
            iv.visitCmp();
        } else if (expression instanceof CmpgExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitCmpg(type1);
        } else if (expression instanceof CmplExpr) {
            requireTypesOfSameKind(type1, type2);
            iv.visitCmpl(type1);
        } else if (expression instanceof ShlExpr) {
            requireEqualTypes(IntType.getInstance(), type2);
            iv.visitShl(type1);
        } else if (expression instanceof ShrExpr) {
            requireEqualTypes(IntType.getInstance(), type2);
            iv.visitShr(type1);
        } else if (expression instanceof UShrExpr) {
            requireEqualTypes(IntType.getInstance(), type2);
            iv.visitUShr(type1);
        } else {
            throw new AssertionError();
        }
    }

    private void requireTypesOfSameKind(Type a, Type b) {
        if (a.equals(b)) {
            return;
        }

        if (a instanceof RefType && b instanceof RefType) {
            return;
        }

        if (a instanceof IntLikeType && b instanceof IntLikeType) {
            return;
        }

        throw new IllegalStateException("Expected values of same type got " + a + " and " + b);
    }

    private void requireEqualTypes(Type expected, Type actual) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Expected value of type " + expected + " got " + actual);
        }
    }

    private void convertVariable(Expression expression) {
        if (expression instanceof InstanceFieldExpr) {
            InstanceFieldExpr fieldExpr = (InstanceFieldExpr) expression;
            accept(fieldExpr.getInstance());
            iv.visitFieldGet(fieldExpr.getField(), true);
        } else if (expression instanceof StaticFieldExpr) {
            StaticFieldExpr fieldExpr = (StaticFieldExpr) expression;
            iv.visitFieldGet(fieldExpr.getField(), false);
        } else if (expression instanceof ArrayBoxExpr) {
            ArrayBoxExpr arrayBoxExpr = (ArrayBoxExpr) expression;
            accept(arrayBoxExpr.getArray());
            accept(arrayBoxExpr.getIndex());
            iv.visitArrayLoad(arrayBoxExpr.getType());
        } else if (expression instanceof RefLocal) {
            StackLocal local = ctx.getStackLocal((RefLocal) expression);
            iv.visitLoad(expression.getType(), local);
        } else {
            throw new AssertionError();
        }
    }

    private void convertCast(CastExpr expression) {
        Type toType = expression.getType();
        Type fromType = expression.getValue().getType();

        if (fromType instanceof RefType && toType instanceof RefType) {
            accept(expression.getValue());
            iv.visitReferenceCast((RefType) toType);
            return;
        }

        if (toType instanceof PrimitiveType && fromType instanceof PrimitiveType) {
            if (toType.equals(fromType)) {
                // casting e.g. float to float cannot be converted to bytecode has would have no effect any.
                return;
            }

            accept(expression.getValue());
            iv.visitPrimitiveCast((PrimitiveType) fromType, (PrimitiveType) toType);
            return;
        }

        throw new IllegalStateException("Cannot cast from " + toType + " to " + fromType);
    }

    private void convertNewArrayExpr(NewArrayExpr expression) {
        List<Expression> dimensions = expression.getDimensionSizes();

        dimensions.forEach(this::accept);
        iv.visitNewArray(expression.getType(), dimensions.size());
    }
}
