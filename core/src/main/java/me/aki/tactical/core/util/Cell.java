package me.aki.tactical.core.util;

public interface Cell {
    /**
     * Get the type of value that this cell points to
     *
     * @return type of the reference behind this cell.
     */
    Class<?> getType();

    /**
     * Check whether a value read from this cell is guaranteed to be of a certain type.
     */
    boolean canRead(Class<?> clazz);

    /**
     * Check whether a value of a certain type can be written into this cell.
     */
    boolean canWrite(Class<?> clazz);

    /**
     * Get a read only cell from which a value of type <tt>T</tt> can be read.
     *
     * @param clazz runtime type of <tt>T</tt>
     * @param <T> type that we want to read
     * @return a safe to use read only cell
     * @throws IllegalStateException casts to type <tt>T</tt> cannot be done.
     * @see Cell#canRead(Class)
     */
    <T> RCell<T> r(Class<T> clazz);

    /**
     * Get a write only cell in which a value of type <tt>T</tt> can be stored.
     *
     * @param clazz runtime type of <tt>T</tt>
     * @param <T> type that we want to write
     * @return a safe to use write only cell
     * @throws IllegalStateException casts to type <tt>T</tt> cannot be done.
     * @see Cell#canWrite(Class)
     */
    <T> WCell<T> w(Class<T> clazz);
}
