package me.aki.tactical.core.util;

import java.util.Collection;
import java.util.List;

/**
 * List that offers insertion- and other kind methods that act relative to other elements in the list.
 *
 * @param <T> type of contained elements
 */
public interface InsertList<T> extends List<T> {
    /**
     * Get the first element of the list.
     *
     * @return first element of the list.
     */
    T getFirst();

    /**
     * Get the last element of the list.
     *
     * @return last element of the list
     */
    T getLast();

    /**
     * Get the element the succeed a certain element.
     *
     * @param element request the element that succeeds this element.
     * @return element that succeeds the given element or <tt>null</tt>.
     */
    T getNext(T element);

    /**
     * Get the element that precedes a certain element.
     *
     * @param element request the precedes that succeeds this element.
     * @return element that precedes the given element or <tt>null</tt>.
     */
    T getPrevious(T element);

    /**
     * Insert an element after another one.
     *
     * @param location insert after this element
     * @param element to be inserted
     */
    void insertAfter(T location, T element);

    /**
     * Insert some elements after another one.
     *
     * @param location insert after this element
     * @param elements to be inserted
     */
    void insertAfter(T location, Collection<T> elements);

    /**
     * Insert an element before another one.
     *
     * @param location insert before this element
     * @param element to be inserted
     */
    void insertBefore(T location, T element);

    /**
     * Insert some elements before another one.
     *
     * @param location insert before this element
     * @param elements to be inserted
     */
    void insertBefore(T location, Collection<T> elements);
}
