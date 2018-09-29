package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public Cell<Expression> getInstanceExpression() {
        return Cell.of(this::getInstance, this::setInstance, Expression.class);
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        List<Cell<Expression>> cells = new ArrayList<>();
        cells.add(getInstanceExpression());
        cells.addAll(getArgumentCells());
        return Collections.unmodifiableList(cells);
    }
}
