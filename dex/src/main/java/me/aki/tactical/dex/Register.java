package me.aki.tactical.dex;

import me.aki.tactical.core.type.Type;

public class Register {
    private Type type;

    public Register(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
