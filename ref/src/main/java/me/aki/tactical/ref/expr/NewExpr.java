package me.aki.tactical.ref.expr;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.ref.Expression;

import java.util.List;

/**
 * Create a new instance of a class.
 *
 * The constructor must be invoked to fully initialize that object.
 */
public class NewExpr implements Expression {
    /**
     * Name of the class whose instance should be created
     */
    private Path path;

    public NewExpr(Path path) {
        this.path = path;
    }

    @Override
    public Type getType() {
        return new ObjectType(path);
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of();
    }
}
