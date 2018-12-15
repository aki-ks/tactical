package me.aki.tactical.core.util;

import java.util.IdentityHashMap;

/**
 * A linked {@link InsertList} that finds elements in the collection based on identity.
 *
 * @param <T> type of elements contained within this list
 * @see LinkedInsertList linked insert list that finds elements based on equality
 */
public class LinkedIdentityInsertList<T> extends AbstractLinkedInsertList<T> {
    public LinkedIdentityInsertList() {
        super(new IdentityHashMap<>());
    }

    public LinkedIdentityInsertList(Iterable<? extends T> elements) {
        super(new IdentityHashMap<>(), elements);
    }
}
