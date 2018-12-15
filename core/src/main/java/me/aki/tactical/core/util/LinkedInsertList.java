package me.aki.tactical.core.util;

import java.util.HashMap;

/**
 * A implementation of {@link InsertList} based on a linked list that finds elements by equality, not identity.
 *
 * @param <T> type of elements in the list
 * @see LinkedIdentityInsertList linked insert list that finds elements based on identity.
 */
public class LinkedInsertList<T> extends AbstractLinkedInsertList<T> {
    public LinkedInsertList() {
        super(new HashMap<>());
    }

    public LinkedInsertList(Iterable<? extends T> elements) {
        super(new HashMap<>(), elements);
    }
}
