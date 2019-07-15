package me.aki.tactical.ref.invoke;

import me.aki.tactical.core.MethodDescriptor;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;
import me.aki.tactical.ref.Referencing;

import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public List<RWCell<Expression>> getArgumentCells() {
        return IntStream.range(0, arguments.size())
                .mapToObj(index -> RWCell.ofList(arguments, index, Expression.class))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get the argument and return type of the invoked method.
     *
     * @return descriptor of the invoked method
     */
    public abstract MethodDescriptor getMethodDescriptor();

    @Override
    public List<RCell<Expression>> getReferencedValueCells() {
        return new ArrayList<>(getArgumentCells());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractInvoke that = (AbstractInvoke) o;
        return Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arguments);
    }
}
