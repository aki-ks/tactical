package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Referencing;

import java.lang.invoke.CallSite;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractInvoke implements Referencing {
    /**
     * Arguments passed to the invoked {@link CallSite}.
     */
    private List<Expression> arguments;

    public AbstractInvoke(List<Expression> arguments) {
        this.arguments = arguments;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public void setArguments(List<Expression> arguments) {
        this.arguments = arguments;
    }

    public List<Cell<Expression>> getArgumentCells() {
        return IntStream.range(0, arguments.size())
                .mapToObj(index -> Cell.ofList(arguments, index, Expression.class))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get the argument and return type of the invoked method.
     *
     * @return descriptor of the invoked method
     */
    public abstract MethodDescriptor getMethodDescriptor();

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return getArgumentCells();
    }
}
