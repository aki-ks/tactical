package me.aki.tactical.core;

import me.aki.tactical.core.type.Type;

public class FieldRef {
    /**
     * Class that contains the field
     */
    private final Path owner;

    /**
     * Name of the field
     */
    private final String name;

    /**
     * Type of the field
     */
    private final Type type;

    public FieldRef(Path owner, String name, Type type) {
        this.owner = owner;
        this.name = name;
        this.type = type;
    }

    public Path getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
