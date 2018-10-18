package me.aki.tactical.ref.textifier;

import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.Textifier;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Variable;
import me.aki.tactical.ref.expr.*;

public class ExpressionTextifier implements Textifier<Expression> {
    public static final Textifier<AbstractBinaryExpr> MATH_EXPR = (printer, expr) -> {
        if (expr instanceof AddExpr) {

        } else if (expr instanceof SubExpr) {

        } else if (expr instanceof MulExpr) {

        } else if (expr instanceof DivExpr) {

        } else if (expr instanceof ModExpr) {

        } else if (expr instanceof AndExpr) {

        } else if (expr instanceof OrExpr) {

        } else if (expr instanceof XorExpr) {

        } else if (expr instanceof CmpExpr) {

        } else if (expr instanceof CmpgExpr) {

        } else if (expr instanceof CmplExpr) {

        } else if (expr instanceof ShlExpr) {

        } else if (expr instanceof ShrExpr) {

        } else if (expr instanceof UShrExpr) {

        } else {
            throw new AssertionError();
        }
    };

    public static final Textifier<Variable> VARIABLE = (printer, expr) -> {
        if (expr instanceof ArrayBoxExpr) {

        } else if (expr instanceof InstanceFieldExpr) {

        } else if (expr instanceof StaticFieldExpr) {

        } else if (expr instanceof RefLocal) {

        } else {
            throw new AssertionError();
        }
    };

    @Override
    public void textify(Printer printer, Expression expr) {
        if (expr instanceof AbstractBinaryExpr) {
            MATH_EXPR.textify(printer, (AbstractBinaryExpr) expr);
        } else if (expr instanceof Variable) {
            VARIABLE.textify(printer, (Variable) expr);
        } else if (expr instanceof ConstantExpr) {

        } else if (expr instanceof ArrayLengthExpr) {

        } else if (expr instanceof CastExpr) {

        } else if (expr instanceof InstanceOfExpr) {

        } else if (expr instanceof InvokeExpr) {

        } else if (expr instanceof NegExpr) {

        } else if (expr instanceof NewExpr) {

        } else if (expr instanceof NewArrayExpr) {

        } else {
            throw new AssertionError();
        }
    }
}
