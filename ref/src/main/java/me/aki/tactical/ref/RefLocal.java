package me.aki.tactical.ref;

import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.util.RCell;
import me.aki.tactical.core.util.RWCell;

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
    public List<RCell<Expression>> getReadValueCells() {
        return List.of();
    }
}
