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
public interface Cell<T> {
    public static <T> Cell<T> of(Supplier<T> getter, Consumer<T> setter) {
        return new Cell<T>() {
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

    public static <T> Cell<T> ofArray(T[] array, int index) {
        return Cell.of(() -> array[index], newValue -> array[index] = newValue);
    }

    public static <T> Cell<T> ofList(List<T> list, int index) {
        return Cell.of(() -> list.get(index), newValue -> list.set(index, newValue));
    }

    public static <T, K> Cell<T> ofMap(K key, Map<K, T> map) {
        return Cell.of(() -> map.get(key), newValue -> map.put(key, newValue));
    }


    /**
     * Get the value behind this reference.
     *
     * @return the value
     */
    public T get();

    /**
     * Set the value behind this reference.
     *
     * @param newValue the value to be set
     */
    public void set(T newValue);
}
