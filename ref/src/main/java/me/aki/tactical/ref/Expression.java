package me.aki.tactical.ref;

import me.aki.tactical.core.type.Type;

public interface Expression extends Referencing {
    /**
     * The type of this value
     */
    Type getType();
}
