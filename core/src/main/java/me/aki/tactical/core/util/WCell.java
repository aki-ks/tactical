package me.aki.tactical.core.util;

/**
 * Write-only view of a {@link RWCell}.
 *
 * @param <T>
 */
public interface WCell<T> extends Cell {
    /**
     * Cast this cell to a subclass of <tt>T</tt>.
     *
     * If values of type <tt>T</tt> can be stored in a cell,
     * all subclasses <tt>U</tt> of <tt>T</tt> can also be stored in that cell.
     * Therefore this method is safe to used and requires no runtime checks
     */
    <U extends T> WCell<U> w();

    void set(T value);
}
