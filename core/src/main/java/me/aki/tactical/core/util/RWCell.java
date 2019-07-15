package me.aki.tactical.core.util;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A mutable reference to a value.
 *
 * This value could be stored in a variable,
 * a list, an array or anywhere else.
 *
 * @param <T> type of the value
 */
public abstract class RWCell<T> implements RCell<T>, WCell<T> {
    /**
     * Create a cell that stores a value in a variable.
     *
     * @param initialValue initial value of the variable
     * @param originalClass runtime {@link Class} of <tt>T</tt>
     * @param <T> Type of the value
     * @return a cell that contains the actual value
     */
    public static <T> RWCell<T> of(T initialValue, Class<T> originalClass) {
        return new RWCell<>(originalClass) {
            private T value = initialValue;

            @Override
            public T get() {
                return value;
            }

            @Override
            public void set(T newValue) {
                this.value = initialValue;
            }
        };
    }

    /**
     * Syntactic sugar for cell initializations.
     *
     * @param getter lambda that gets the value
     * @param setter lambda that reassigns the value
     * @param originalClass runtime {@link Class} of <tt>T</tt>
     * @param <T> Type of the value
     * @return a cell that uses the provided lambdas
     */
    public static <T> RWCell<T> of(Supplier<T> getter, Consumer<T> setter, Class<T> originalClass) {
        return new RWCell<>(originalClass) {
            @Override
            public T get() {
                return getter.get();
            }

            @Override
            public void set(T newValue) {
                setter.accept(newValue);
            }
        };
    }

    /**
     * Cell that references a value within an array.
     *
     * @param array that contains the value
     * @param index of the value in the array
     * @param originalClass runtime {@link Class} of <tt>T</tt>
     * @param <T> Type of the value
     * @return cell that references an element in an array
     */
    public static <T> RWCell<T> ofArray(T[] array, int index, Class<T> originalClass) {
        return RWCell.of(() -> array[index], newValue -> array[index] = newValue, originalClass);
    }

    /**
     * Cell that references a value within a {@link List}.
     *
     * @param list that contains the value
     * @param index of the value in the list
     * @param originalClass runtime {@link Class} of <tt>T</tt>
     * @param <T> Type of the value
     * @return cell that references an element in a list
     */
    public static <T> RWCell<T> ofList(List<T> list, int index, Class<T> originalClass) {
        return RWCell.of(() -> list.get(index), newValue -> list.set(index, newValue), originalClass);
    }

    /**
     * Cell that references the value that a {@link Map} assigns to a key.
     *
     * @param key to which the value is assigned
     * @param map that contains the referenced value
     * @param originalClass runtime {@link Class} of <tt>T</tt>
     * @param <T> Type of the value
     * @return cell that references a value in a map
     */
    public static <T, K> RWCell<T> ofMap(K key, Map<K, T> map, Class<T> originalClass) {
        return RWCell.of(() -> map.get(key), newValue -> map.put(key, newValue), originalClass);
    }

    /**
     * Runtime type of <tt>T</tt>.
     */
    private final Class<T> type;

    public RWCell(Class<T> type) {
        this.type = type;
    }

    /**
     * Get the value behind this reference.
     *
     * @return the value
     */
    public abstract T get();

    /**
     * Set the value behind this reference.
     *
     * @param newValue the value to be set
     */
    public abstract void set(T newValue);

    public final Class<T> getType() {
        return type;
    }

    public boolean canWrite(Class<?> clazz) {
        return type.isAssignableFrom(clazz);
    }

    public boolean canRead(Class<?> clazz) {
        return clazz.isAssignableFrom(type);
    }

    public final <U> RCell<U> r(Class<U> clazz) {
        if (canRead(clazz)) {
            return (RCell<U>) this;
        } else {
            throw new IllegalStateException("Type " + clazz + " cannot be read from a Cell<" + type + ">");
        }
    }

    public final <U> WCell<U> w(Class<U> clazz) {
        if (canWrite(clazz)) {
            return (WCell<U>) this;
        } else {
            throw new IllegalStateException("Type " + clazz + " cannot be stored in a Cell<" + type + ">");
        }
    }

    @Override
    public <U extends T> WCell<U> w() {
        return (WCell<U>) this;
    }
}
