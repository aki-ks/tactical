package me.aki.tactical.ref.expr;

import me.aki.tactical.core.type.ArrayType;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;
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

    public List<Cell<Expression>> getDimensionSizeCells() {
        return IntStream.range(0, dimensionSizes.size())
                .mapToObj(index -> Cell.ofList(dimensionSizes, index))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.copyOf(getDimensionSizeCells());
    }
}
