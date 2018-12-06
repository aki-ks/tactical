package me.aki.tactical.conversion.ref2stack;

import me.aki.tactical.core.constant.IntConstant;
import me.aki.tactical.core.constant.NullConstant;
import me.aki.tactical.core.constant.PushableConstant;
import me.aki.tactical.core.type.*;
import me.aki.tactical.ref.*;
import me.aki.tactical.ref.condition.*;
import me.aki.tactical.ref.expr.*;
import me.aki.tactical.ref.invoke.*;
import me.aki.tactical.ref.stmt.*;
import me.aki.tactical.stack.insn.IfInsn;
import me.aki.tactical.stack.invoke.DynamicInvoke;
import me.aki.tactical.stack.invoke.InterfaceInvoke;
import me.aki.tactical.stack.invoke.Invoke;
import me.aki.tactical.stack.invoke.SpecialInvoke;
import me.aki.tactical.stack.invoke.StaticInvoke;
import me.aki.tactical.stack.invoke.VirtualInvoke;
import me.aki.tactical.stack.utils.StackInsnVisitor;

import java.util.List;
import java.util.Optional;

/**
 * Utility that calls events corresponding to {@link Statement Statements} or
 * {@link Expression Expressions} on a {@link StackInsnVisitor}.
 */
public class RefInsnReader {
    private final StackInsnVisitor<Statement, RefLocal> iv;

    private static <T> T assertionError() {
        throw new AssertionError();
    }

    public RefInsnReader(StackInsnVisitor<Statement, RefLocal> iv) {
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
            convertInvokeStatement((InvokeStmt) statement);
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
        Type type1 = value1.getType();
        Type type2 = value2.getType();

        accept(value1);

        IfInsn.Condition stackCondition;
        if (type1 instanceof IntLikeType) {
            if (!(type2 instanceof IntLikeType)) {
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
        } else if (type1 instanceof RefType) {
            if (!(type2 instanceof RefType)) {
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
            convertVariableAssignment((RefLocal) variable, value);
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

    private void convertVariableAssignment(RefLocal variable, Expression value) {
        // Try to generate an increment instruction if possible
        if (value.getType() instanceof IntLikeType) {
            if (value instanceof AddExpr) {
                AddExpr add = (AddExpr) value;

                if (add.getValue1() == variable && add.getValue2() instanceof ConstantExpr) {
                    PushableConstant constant = ((ConstantExpr) add.getValue2()).getConstant();
                    if (constant instanceof IntConstant) {
                        iv.visitIncrement(variable, ((IntConstant) constant).getValue());
                        return;
                    }
                }

                if (add.getValue2() == variable && add.getValue1() instanceof ConstantExpr) {
                    PushableConstant constant = ((ConstantExpr) add.getValue1()).getConstant();
                    if (constant instanceof IntConstant) {
                        iv.visitIncrement(variable, ((IntConstant) constant).getValue());
                        return;
                    }
                }
            } else if (value instanceof SubExpr) {
                SubExpr sub = (SubExpr) value;

                if (sub.getValue1() == value && sub.getValue2() instanceof ConstantExpr) {
                    PushableConstant constant = ((ConstantExpr) sub.getValue2()).getConstant();
                    if (constant instanceof IntConstant) {
                        iv.visitIncrement(variable, -(((IntConstant) constant).getValue()));
                        return;
                    }
                }
            }
        }

        accept(value);
        iv.visitStore(value.getType(), variable);
    }

    private void convertInvokeStatement(InvokeStmt statement) {
        convertInvoke(statement.getInvoke());

        Optional<Type> returnType = statement.getInvoke().getMethodDescriptor().getReturnType();
        if (returnType.isPresent()) {
            // the returned value of the invoke expressions is not used.
            iv.visitPop();
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
            iv.visitFieldGet(fieldExpr.getField(), false);
        } else if (expression instanceof StaticFieldExpr) {
            StaticFieldExpr fieldExpr = (StaticFieldExpr) expression;
            iv.visitFieldGet(fieldExpr.getField(), true);
        } else if (expression instanceof ArrayBoxExpr) {
            ArrayBoxExpr arrayBoxExpr = (ArrayBoxExpr) expression;
            accept(arrayBoxExpr.getArray());
            accept(arrayBoxExpr.getIndex());
            iv.visitArrayLoad(arrayBoxExpr.getType());
        } else if (expression instanceof RefLocal) {
            iv.visitLoad(expression.getType(), (RefLocal) expression);
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
