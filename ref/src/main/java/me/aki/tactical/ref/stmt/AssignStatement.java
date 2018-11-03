package me.aki.tactical.ref.stmt;

import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Statement;
import me.aki.tactical.ref.Variable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Assign a value to a variable
 */
public class AssignStatement implements Statement {
    /**
     * Variable that will be reassigned
     */
    private Variable variable;

    /**
     * Value to be assigned to the variable.
     */
    private Expression value;

    public AssignStatement(Variable variable, Expression value) {
        this.variable = variable;
        this.value = value;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public Cell<Variable> getVariableCell() {
        return Cell.of(this::getVariable, this::setVariable, Variable.class);
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public Cell<Expression> getValueCell() {
        return Cell.of(this::getValue, this::setValue, Expression.class);
    }

    @Override
    public List<Cell<Expression>> getReferencedValueCells() {
        return List.of(getVariableCell().cast(), getValueCell());
    }

    @Override
    public List<Expression> getReadValues() {
        return Stream.concat(Stream.of(getValue()), getValue().getRecursiveReferencedValues().stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Cell<Expression>> getReadValueCells() {
        return Stream.concat(Stream.of(getValueCell()), getValue().getRecursiveReferencedValueCells().stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<Variable> getWriteValues() {
        return Optional.of(getVariable());
    }

    @Override
    public Optional<Cell<Variable>> getWriteValueCells() {
        return Optional.of(getVariableCell());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignStatement statement = (AssignStatement) o;
        return Objects.equals(variable, statement.variable) &&
                Objects.equals(value, statement.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, value);
    }
}
