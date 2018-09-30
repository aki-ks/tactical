package me.aki.tactical.ref;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.Cell;

import java.util.List;

public class RefLocal implements Variable {
    /**
     * Type of this Local
     */
    private Type type;

    public RefLocal(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public List<Cell<Expression>> getReferencedValues() {
        return List.of();
    }
}

