package me.aki.tactical.ref;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RCell;

import java.util.Set;

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
    public Set<RCell<Expression>> getReadValueCells() {
        return Set.of();
    }
}
