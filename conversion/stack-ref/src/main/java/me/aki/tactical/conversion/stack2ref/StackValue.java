package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.RefLocal;
import me.aki.tactical.ref.stmt.AssignStmt;
import me.aki.tactical.stack.insn.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An expression that represents a value on the stack and all references to it.
 */
public class StackValue {
    /**
     * Instruction that created the value.
     */
    private Instruction instruction;

    private Optional<AssignStmt> assignStatement = Optional.empty();

    /**
     * The actual value.
     */
    private Expression value;

    /**
     * All cells that refer to the expression.
     */
    private List<Cell<Expression>> references = new ArrayList<>();

    public StackValue(Instruction instruction, Expression value) {
        this.instruction = instruction;
        this.value = value;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public Optional<AssignStmt> getAssignStatement() {
        return assignStatement;
    }

    public Expression getValue() {
        return value;
    }

    /**
     * Change the expression and thereby update all references to it.
     *
     * @param value the new expression
     */
    public void setValue(Expression value) {
        this.value = value;
        this.references.forEach(cell -> cell.set(value));
    }

    public List<Cell<Expression>> getReferences() {
        return references;
    }

    public void addReference(Cell<Expression> cell) {
        this.references.add(cell);
    }

    /**
     * Create a {@link AssignStmt} that stores the expression in a local.
     * All references to the expression will get replaced against the local.
     *
     * @param converter that currently converts the instructions
     * @param local that will get assigned to that statement
     * @return statement that stores the value in the local
     */
    public AssignStmt storeInLocal(BodyConverter converter, RefLocal local) {
        Expression oldValue = this.value;
        this.setValue(local);

        if (this.assignStatement.isPresent()) {
            AssignStmt assignStmt = this.assignStatement.get();
            assignStmt.setVariable(local);
            return assignStmt;
        } else {
            AssignStmt assignment = new AssignStmt(local, oldValue);
            this.assignStatement = Optional.of(assignment);

            converter.getConvertedStatements()
                    .computeIfAbsent(this.instruction, x -> new ArrayList<>())
                    .add(assignment);

            return assignment;
        }
    }
}
