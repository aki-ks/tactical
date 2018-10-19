package me.aki.tactical.ref.textifier;

import me.aki.tactical.core.textify.Printer;
import me.aki.tactical.core.textify.Textifier;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.stmt.*;

public class StatementTextifier implements Textifier<Statement> {
    private static final StatementTextifier INSTANCE = new StatementTextifier();

    public static StatementTextifier getInstance() {
        return INSTANCE;
    }

    @Override
    public void textify(Printer printer, Statement statement) {
        if (statement instanceof BranchStmt) {
            if (statement instanceof GotoStmt) {

            } else if (statement instanceof IfStmt) {

            } else if (statement instanceof SwitchStmt) {

            } else {
                throw new AssertionError();
            }
        } else if (statement instanceof AssignStatement) {

        } else if (statement instanceof InvokeStmt) {

        } else if (statement instanceof ReturnStmt) {

        } else if (statement instanceof ThrowStmt) {

        } else if (statement instanceof MonitorEnterStmt) {

        } else if (statement instanceof MonitorExitStmt) {

        } else {
            throw new AssertionError();
        }
    }
}
