package me.aki.tactical.ref.expr;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.ref.Expression;

import java.util.List;
import java.util.Objects;

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

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public Type getType() {
        return new ObjectType(path);
    }

    @Override
    public List<RCell<Expression>> getReadValueCells() {
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewExpr expr = (NewExpr) o;
        return Objects.equals(path, expr.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
