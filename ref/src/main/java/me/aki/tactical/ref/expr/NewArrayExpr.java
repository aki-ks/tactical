package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create and initialize an array.
 */
public class NewArrayExpr implements Expression {
    /**
     * Type of the array to be initialized
     */
    private ArrayType type;

    /**
     * The sizes of the dimensions of that array that should be initialized.
     * At least one dimension must be initialized for any array type.
     */
    private List<Expression> dimensionSizes;

    public NewArrayExpr(ArrayType type, List<Expression> dimensionSizes) {
        this.type = type;
        this.dimensionSizes = dimensionSizes;
    }

    @Override
    public ArrayType getType() {
        return type;
    }

    public void setType(ArrayType type) {
        this.type = type;
    }

    public List<Expression> getDimensionSizes() {
        return dimensionSizes;
    }

    public void setDimensionSizes(List<Expression> dimensionSizes) {
        this.dimensionSizes = dimensionSizes;
    }

    public List<RWCell<Expression>> getDimensionSizeCells() {
        return IntStream.range(0, dimensionSizes.size())
                .mapToObj(index -> RWCell.ofList(dimensionSizes, index, Expression.class))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Set<RCell<Expression>> getReadValueCells() {
        return Set.copyOf(getDimensionSizeCells());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewArrayExpr expr = (NewArrayExpr) o;
        return Objects.equals(type, expr.type) &&
                Objects.equals(dimensionSizes, expr.dimensionSizes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, dimensionSizes);
    }
}
