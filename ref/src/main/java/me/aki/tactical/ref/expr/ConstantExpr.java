package me.aki.tactical.ref.expr;

import me.aki.tactical.core.constant.PushableConstant;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.Objects;

/**
 * Reference the value of a constant.
 */
public class ConstantExpr implements Expression {
    private PushableConstant constant;

    public ConstantExpr(PushableConstant constant) {
        this.constant = constant;
    }

    public PushableConstant getConstant() {
        return constant;
    }

    public void setConstant(PushableConstant constant) {
        this.constant = constant;
    }

    @Override
    public Type getType() {
        return constant.getType();
    }

    @Override
    public List<RCell<Expression>> getReferencedValueCells() {
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantExpr that = (ConstantExpr) o;
        return Objects.equals(constant, that.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constant);
    }
}
