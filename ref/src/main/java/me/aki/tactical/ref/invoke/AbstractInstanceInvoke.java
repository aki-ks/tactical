package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Invoke a method of an instance of a class.
 */
public class AbstractInstanceInvoke extends AbstractConcreteInvoke {
    private Expression instance;

    public AbstractInstanceInvoke(MethodRef method, Expression instance, List<Expression> arguments) {
        super(method, arguments);
        this.instance = instance;
    }

    public Expression getInstance() {
        return instance;
    }

    public void setInstance(Expression instance) {
        this.instance = instance;
    }

    public Cell<Expression> getInstanceCell() {
        return Cell.of(this::getInstance, this::setInstance, Expression.class);
    }

    @Override
    public List<Cell<Expression>> getReferencedValueCells() {
        List<Cell<Expression>> cells = new ArrayList<>();
        cells.add(getInstanceCell());
        cells.addAll(getArgumentCells());
        return Collections.unmodifiableList(cells);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractInstanceInvoke that = (AbstractInstanceInvoke) o;
        return Objects.equals(instance, that.instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instance);
    }
}
