package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.Variable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Assign a value to a variable
 */
public class AssignStmt implements Statement {
    /**
     * Variable that will be reassigned
     */
    private Variable variable;

    /**
     * Value to be assigned to the variable.
     */
    private Expression value;

    public AssignStmt(Variable variable, Expression value) {
        this.variable = variable;
        this.value = value;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public RWCell<Variable> getVariableCell() {
        return RWCell.of(this::getVariable, this::setVariable, Variable.class);
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public RWCell<Expression> getValueCell() {
        return RWCell.of(this::getValue, this::setValue, Expression.class);
    }

    @Override
    public List<RCell<Expression>> getReferencedValueCells() {
        return List.of(getVariableCell().r(Expression.class), getValueCell());
    }

    @Override
    public List<Expression> getReadValues() {
        return Stream.concat(Stream.of(getValue()), getValue().getRecursiveReferencedValues().stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<RCell<Expression>> getReadValueCells() {
        return Stream.concat(Stream.of(getValueCell()), getValue().getRecursiveReferencedValueCells().stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<Variable> getWriteValue() {
        return Optional.of(getVariable());
    }

    @Override
    public Optional<RWCell<Variable>> getWriteValueCell() {
        return Optional.of(getVariableCell());
    }
}
