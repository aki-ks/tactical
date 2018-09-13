package me.aki.tactical.core;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a not parsed attribute.
 */
public class Attribute {
    /**
     * Name of this attribute
     */
    private final String name;

    /**
     * Unparsed data of this attribute
     */
    private final byte[] data;

    public Attribute(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return Objects.equals(name, attribute.name) &&
                Arrays.equals(data, attribute.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "name='" + name + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
