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
public abstract class Cell<T> {
    /**
     * Create a cell that stores a in a variable.
     *
     * @param initialValue initial value of the variable
     * @param originalClass runtime {@link Class} of <tt>T</tt>
     * @param <T> Type of the value
     * @return a cell that contains the actual value
     */
    public static <T> Cell<T> of (T initialValue, Class<T> originalClass) {
        return new Cell<>(originalClass) {
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
    public static <T> Cell<T> of(Supplier<T> getter, Consumer<T> setter, Class<T> originalClass) {
        return new Cell<>(originalClass) {
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
    public static <T> Cell<T> ofArray(T[] array, int index, Class<T> originalClass) {
        return Cell.of(() -> array[index], newValue -> array[index] = newValue, originalClass);
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
    public static <T> Cell<T> ofList(List<T> list, int index, Class<T> originalClass) {
        return Cell.of(() -> list.get(index), newValue -> list.set(index, newValue), originalClass);
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
    public static <T, K> Cell<T> ofMap(K key, Map<K, T> map, Class<T> originalClass) {
        return Cell.of(() -> map.get(key), newValue -> map.put(key, newValue), originalClass);
    }

    /**
     * Class instance of <tt>T</tt> in the original (non-casted) box.
     */
    private final Class<?> originalClass;

    public Cell(Class<T> originalClass) {
        this.originalClass = originalClass;
    }

    // 'dummy' argument is necessary to bypass type erasure
    private Cell(Class<?> originalClass, Object dummy) {
        this.originalClass = originalClass;
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

    /**
     * Get the runtime class of <tt>T</tt> of the non-casted box.
     *
     * @return original type of <tt>T</tt>
     */
    public final Class<?> getOriginalType() {
        return originalClass;
    }

    /**
     * Cast this cell to another value type.
     * It's runtime checked that the value stays of the original type <tt>T</tt>.
     *
     * @param <U> new element type
     * @return this cell with another <tt>T</tt> type
     */
    public final <U> Cell<U> cast() {
        return new Cell<>(originalClass, null) {
            @Override
            public U get() {
                return (U) Cell.this.get();
            }

            @Override
            public void set(U newValue) {
                if (newValue == null || originalClass.isAssignableFrom(newValue.getClass())) {
                    Cell.this.set((T) newValue);
                } else {
                    String wrongType = newValue.getClass().getName();
                    String expectedType = originalClass.getName();
                    throw new IllegalArgumentException("Cannot store a " + wrongType + " in a Cell<" + expectedType + ">");
                }
            }
        };
    }
}
