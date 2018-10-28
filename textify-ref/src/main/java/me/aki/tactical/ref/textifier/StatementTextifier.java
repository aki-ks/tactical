package me.aki.tactical.ref.textifier;

import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.TextUtil;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.condition.Condition;
import me.aki.tactical.ref.condition.Equal;
import me.aki.tactical.ref.condition.GreaterEqual;
import me.aki.tactical.ref.condition.GreaterThan;
import me.aki.tactical.ref.condition.LessEqual;
import me.aki.tactical.ref.condition.LessThan;
import me.aki.tactical.ref.condition.NonEqual;
import me.aki.tactical.ref.stmt.*;

import java.util.Objects;

public class StatementTextifier implements CtxTextifier<Statement> {
    private static final StatementTextifier INSTANCE = new StatementTextifier();

    public static StatementTextifier getInstance() {
        return INSTANCE;
    }

    public static final CtxTextifier<GotoStmt> GOTO = (printer, ctx, statement) -> {
        printer.addText("goto ");
        printer.addLiteral(ctx.getLabel(statement.getTarget()));
        printer.addText(";");
    };

    public static final CtxTextifier<IfStmt> IF = (printer, ctx, statement) -> {
        printer.addText("if (");

        Condition condition = statement.getCondition();
        ExpressionTextifier.getInstance().textify(printer, ctx, condition.getValue1());
        printer.addText(" " + getSymbol(condition) + " ");
        ExpressionTextifier.getInstance().textify(printer, ctx, condition.getValue2());

        printer.addText(") goto ");
        printer.addLiteral(ctx.getLabel(statement.getTarget()));
        printer.addText(";");
    };

    private static String getSymbol(Condition condition) {
        return condition instanceof Equal ? "==" :
                condition instanceof NonEqual ? "!=" :
                condition instanceof LessThan ? "<" :
                condition instanceof LessEqual ? "<=" :
                condition instanceof GreaterThan ? ">" :
                condition instanceof GreaterEqual ? ">=" :
                        TextUtil.assertionError();
    }

    public static final CtxTextifier<SwitchStmt> SWITCH = (printer, ctx, statement) -> {
        printer.addText("switch (");
        ExpressionTextifier.getInstance().textify(printer, ctx, statement.getValue());
        printer.addText(") {");
        printer.newLine();
        printer.increaseIndent();

        statement.getBranchTable().forEach((key, target) -> {
            printer.addText("case " + key + ": goto ");
            printer.addLiteral(ctx.getLabel(target));
            printer.addText(";");
            printer.newLine();
        });

        printer.addText("default: goto ");
        printer.addLiteral(ctx.getLabel(statement.getDefaultTarget()));
        printer.addText(";");
        printer.newLine();

        printer.decreaseIndent();
        printer.addText("};");
    };

    public static final CtxTextifier<AssignStatement> ASSIGN = (printer, ctx, statement) -> {
        ExpressionTextifier.VARIABLE.textify(printer, ctx, statement.getVariable());
        printer.addText(" = ");
        ExpressionTextifier.getInstance().textify(printer, ctx, statement.getValue());
        printer.addText(";");
    };

    public static final CtxTextifier<InvokeStmt> INVOKE = (printer, ctx, statement) -> {
        InvokeTextifier.getInstance().textify(printer, ctx, statement.getInvoke());
        printer.addText(";");
    };

    public static final CtxTextifier<ReturnStmt> RETURN = (printer, ctx, statement) -> {
        statement.getValue().ifPresentOrElse(value -> {
            printer.addText("return ");
            ExpressionTextifier.getInstance().textify(printer, ctx, value);
            printer.addText(";");
        }, () -> printer.addText("return;"));
    };

    public static final CtxTextifier<ThrowStmt> THROW = (printer, ctx, statement) -> {
        printer.addText("throw ");
        ExpressionTextifier.getInstance().textify(printer, ctx, statement.getValue());
        printer.addText(";");
    };

    private  static final CtxTextifier<MonitorEnterStmt> MONITOR_ENTER = (printer, ctx, statement) -> {
        printer.addText("monitor enter ");
        ExpressionTextifier.getInstance().textify(printer, ctx, statement.getValue());
        printer.addText(";");
    };

    private  static final CtxTextifier<MonitorExitStmt> MONITOR_EXIT = (printer, ctx, statement) -> {
        printer.addText("monitor exit ");
        ExpressionTextifier.getInstance().textify(printer, ctx, statement.getValue());
        printer.addText(";");
    };

    @Override
    public void textify(Printer printer, TextifyCtx ctx, Statement statement) {
        if (statement instanceof BranchStmt) {
            if (statement instanceof GotoStmt) {
                GOTO.textify(printer, ctx, (GotoStmt) statement);
            } else if (statement instanceof IfStmt) {
                IF.textify(printer, ctx, (IfStmt) statement);
            } else if (statement instanceof SwitchStmt) {
                SWITCH.textify(printer, ctx, (SwitchStmt) statement);
            } else {
                throw new AssertionError();
            }
        } else if (statement instanceof AssignStatement) {
            ASSIGN.textify(printer, ctx, (AssignStatement) statement);
        } else if (statement instanceof InvokeStmt) {
            INVOKE.textify(printer, ctx, (InvokeStmt) statement);
        } else if (statement instanceof ReturnStmt) {
            RETURN.textify(printer, ctx, (ReturnStmt) statement);
        } else if (statement instanceof ThrowStmt) {
            THROW.textify(printer, ctx, (ThrowStmt) statement);
        } else if (statement instanceof MonitorEnterStmt) {
            MONITOR_ENTER.textify(printer, ctx, (MonitorEnterStmt) statement);
        } else if (statement instanceof MonitorExitStmt) {
            MONITOR_EXIT.textify(printer, ctx, (MonitorExitStmt) statement);
        } else {
            throw new AssertionError();
        }
    }
}
