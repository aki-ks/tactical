package me.aki.tactical.ref.textifier;

import me.aki.tactical.core.FieldRef;
import me.aki.tactical.core.textify.ConstantTextifier;
import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.Textifier;
import me.aki.tactical.core.textify.TypeTextifier;
import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.Variable;
import me.aki.tactical.ref.expr.*;
import me.aki.tactical.ref.invoke.AbstractInvoke;

import java.util.Iterator;
import java.util.Optional;

public class ExpressionTextifier implements CtxTextifier<Expression> {
    private static final ExpressionTextifier INSTANCE = new ExpressionTextifier();

    public static ExpressionTextifier getInstance() {
        return INSTANCE;
    }

    private static void textifyMathExpr(Printer printer, TextifyCtx ctx, AbstractBinaryExpr expr, String symbol) {
        getInstance().textify(printer, ctx, expr.getValue1());
        printer.addText(" " + symbol + " ");
        getInstance().textify(printer, ctx, expr.getValue2());
    }

    public static final CtxTextifier<AbstractBinaryExpr> MATH_EXPR = (printer, ctx, expr) -> {
        if (expr instanceof AddExpr) {
            textifyMathExpr(printer, ctx, expr, "+");
        } else if (expr instanceof SubExpr) {
            textifyMathExpr(printer, ctx, expr, "-");
        } else if (expr instanceof MulExpr) {
            textifyMathExpr(printer, ctx, expr, "*");
        } else if (expr instanceof DivExpr) {
            textifyMathExpr(printer, ctx, expr, "/");
        } else if (expr instanceof ModExpr) {
            textifyMathExpr(printer, ctx, expr, "%");
        } else if (expr instanceof AndExpr) {
            textifyMathExpr(printer, ctx, expr, "&");
        } else if (expr instanceof OrExpr) {
            textifyMathExpr(printer, ctx, expr, "|");
        } else if (expr instanceof XorExpr) {
            textifyMathExpr(printer, ctx, expr, "^");
        } else if (expr instanceof CmpExpr) {
            textifyMathExpr(printer, ctx, expr, "cmp");
        } else if (expr instanceof CmpgExpr) {
            textifyMathExpr(printer, ctx, expr, "cmpg");
        } else if (expr instanceof CmplExpr) {
            textifyMathExpr(printer, ctx, expr, "cmpl");
        } else if (expr instanceof ShlExpr) {
            textifyMathExpr(printer, ctx, expr, "<<");
        } else if (expr instanceof ShrExpr) {
            textifyMathExpr(printer, ctx, expr, ">>");
        } else if (expr instanceof UShrExpr) {
            textifyMathExpr(printer, ctx, expr, ">>>");
        } else {
            throw new AssertionError();
        }
    };

    public static final CtxTextifier<ArrayBoxExpr> ARRAY_BOX = (printer, ctx, expr) -> {
        Expression array = expr.getArray();

        // immediate accesses of just created arrays would get parsed as accesses of multidimensional arrays.
        // e.g. (new int[4]) [0]
        boolean needsBrackets = array instanceof NewArrayExpr;

        if (needsBrackets)printer.addText("(");
        getInstance().textify(printer, ctx, array);
        if (needsBrackets)printer.addText(")");
        printer.addText("[");
        getInstance().textify(printer, ctx, expr.getIndex());
        printer.addText("]");
    };

    private static void textifyFieldExpr(Printer printer, TextifyCtx ctx, FieldRef field, Optional<Expression> instanceOpt) {
        printer.addPath(field.getOwner());
        printer.addText(".");
        instanceOpt.ifPresent(instance -> {
            printer.addText("<");
            getInstance().textify(printer, ctx, instance);
            printer.addText(">.");
        });

        printer.addLiteral(field.getName());
        printer.addText(" : ");
        TypeTextifier.getInstance().textify(printer, field.getType());
    }

    public static final CtxTextifier<InstanceFieldExpr> INSTANCE_FIELD = (printer, ctx, expr) -> {
        textifyFieldExpr(printer, ctx, expr.getField(), Optional.of(expr.getInstance()));
    };

    public static final CtxTextifier<StaticFieldExpr> STATIC_FIELD = (printer, ctx, expr) -> {
        textifyFieldExpr(printer, ctx, expr.getField(), Optional.empty());
    };

    public static final CtxTextifier<RefLocal> LOCAL = (printer, ctx, local) -> {
        printer.addLiteral(ctx.getLocalName(local));
    };

    public static final CtxTextifier<Variable> VARIABLE = (printer, ctx, expr) -> {
        if (expr instanceof ArrayBoxExpr) {
            ARRAY_BOX.textify(printer, ctx, (ArrayBoxExpr) expr);
        } else if (expr instanceof InstanceFieldExpr) {
            INSTANCE_FIELD.textify(printer, ctx, (InstanceFieldExpr) expr);
        } else if (expr instanceof StaticFieldExpr) {
            STATIC_FIELD.textify(printer, ctx, (StaticFieldExpr) expr);
        } else if (expr instanceof RefLocal) {
            LOCAL.textify(printer, ctx, (RefLocal) expr);
        } else {
            throw new AssertionError();
        }
    };

    public static final Textifier<ConstantExpr> CONSTANT = (printer, expr) -> {
        ConstantTextifier.PUSHABLE.textify(printer, expr.getConstant());
    };

    public static final CtxTextifier<ArrayLengthExpr> ARRAY_LENGTH = (printer, ctx, value) -> {
        getInstance().textify(printer, ctx, value.getArray());
        printer.addText(".length");
    };

    public static final CtxTextifier<CastExpr> CAST = (printer, ctx, expr) -> {
        printer.addText("(");
        TypeTextifier.getInstance().textify(printer, expr.getType());
        printer.addText(") ");
        getInstance().textify(printer, ctx, expr.getValue());
    };

    public static final CtxTextifier<InstanceOfExpr> INSTANCE_OF = (printer, ctx, expr) -> {
        getInstance().textify(printer, ctx, expr.getValue());
        printer.addText(" instanceof ");
        TypeTextifier.REF_TYPE.textify(printer, expr.getCheckType());
    };

    public static final CtxTextifier<InvokeExpr> INVOKE = (printer, ctx, expr) -> {
        AbstractInvoke invokation = expr.getInvocation();
        InvokeTextifier.getInstance().textify(printer, ctx, invokation);
    };

    public static final CtxTextifier<NegExpr> NEG = (printer, ctx, expr) -> {
        printer.addText("-(");
        getInstance().textify(printer, ctx, expr.getValue());
        printer.addText(")");
    };

    public static final Textifier<NewExpr> NEW = (printer, expr) -> {
        printer.addText("new ");
        printer.addPath(expr.getPath());
    };

    public static final CtxTextifier<NewArrayExpr> NEW_ARRAY = (printer, ctx, expr) -> {
        printer.addText("new ");
        ArrayType type = expr.getType();
        TypeTextifier.getInstance().textify(printer, type.getBaseType());

        Iterator<Expression> iter = expr.getDimensionSizes().iterator();
        for (int i = 0; i < type.getDimensions(); i++) {
            if (iter.hasNext()) {
                printer.addText("[");
                getInstance().textify(printer, ctx, iter.next());
                printer.addText("]");
            } else {
                printer.addText("[]");
            }
        }
    };

    @Override
    public void textify(Printer printer, TextifyCtx ctx, Expression expr) {
        if (expr instanceof AbstractBinaryExpr) {
            MATH_EXPR.textify(printer, ctx, (AbstractBinaryExpr) expr);
        } else if (expr instanceof Variable) {
            VARIABLE.textify(printer, ctx, (Variable) expr);
        } else if (expr instanceof ConstantExpr) {
            CONSTANT.textify(printer, (ConstantExpr) expr);
        } else if (expr instanceof ArrayLengthExpr) {
            ARRAY_LENGTH.textify(printer, ctx, (ArrayLengthExpr) expr);
        } else if (expr instanceof CastExpr) {
            CAST.textify(printer, ctx, (CastExpr) expr);
        } else if (expr instanceof InstanceOfExpr) {
            INSTANCE_OF.textify(printer, ctx, (InstanceOfExpr) expr);
        } else if (expr instanceof InvokeExpr) {
            INVOKE.textify(printer, ctx, (InvokeExpr) expr);
        } else if (expr instanceof NegExpr) {
            NEG.textify(printer, ctx, (NegExpr) expr);
        } else if (expr instanceof NewExpr) {
            NEW.textify(printer, (NewExpr) expr);
        } else if (expr instanceof NewArrayExpr) {
            NEW_ARRAY.textify(printer, ctx, (NewArrayExpr) expr);
        } else {
            throw new AssertionError();
        }
    }
}
