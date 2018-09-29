package me.aki.tactical.ref.expr;

import me.aki.tactical.core.constant.PushableConstant;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;

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
    public List<Cell<Expression>> getReferencedValues() {
        return List.of();
    }
}
