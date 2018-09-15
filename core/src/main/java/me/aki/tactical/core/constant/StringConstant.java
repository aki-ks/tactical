package me.aki.tactical.core.constant;

import java.util.Objects;

/**
 * An instance of "java.lang.String".
 */
public class StringConstant implements FieldConstant {
    private final String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        me.aki.tactical.core.constant.StringConstant that = (me.aki.tactical.core.constant.StringConstant) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return me.aki.tactical.core.constant.StringConstant.class.getSimpleName() + '{' +
                "value='" + value + '\'' +
                '}';
    }
}
