package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.MethodRef;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Invoke a statically known method.
 */
public abstract class AbstractConcreteInvoke implements Invoke {
    /**
     * Method that will be invoked
     */
    private MethodRef method;

    /**
     * Arguments passed to the method
     */
    private List<Expression> arguments;

    public AbstractConcreteInvoke(MethodRef method, List<Expression> arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    public MethodRef getMethod() {
        return method;
    }

    public void setMethod(MethodRef method) {
        this.method = method;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public void setArguments(List<Expression> arguments) {
        this.arguments = arguments;
    }

    @Override
    public MethodDescriptor getMethodDescriptor() {
        return new MethodDescriptor(method.getArguments(), method.getReturnType());
    }

    public List<Cell<Expression>> getArgumentCells() {
        return IntStream.range(0, arguments.size())
                .mapToObj(index -> Cell.ofList(arguments, index, Expression.class))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return getArgumentCells();
    }
}
