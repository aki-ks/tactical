package me.aki.tactical.core.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A linked {@link InsertList} that maintains a {@link HashMap} from all elements to their nodes.
 * Therefore it can contains each element only once.
 *
 * @param <T> type of the stored elements
 */
public class AbstractLinkedInsertList<T> extends AbstractList<T> implements InsertList<T> {
    /**
     * Node of the first element in the list or <tt>null</tt>.
     */
    private Node first;

    /**
     * Node of the last element in the list or <tt>null</tt>.
     */
    private Node last;

    /**
     * Map from all values in the list to their corresponding {@link Node}.
     */
    private Map<T, Node> nodes;

    AbstractLinkedInsertList(Map<T, Node> nodeMap) {
        this.nodes = nodeMap;
        this.first = null;
        this.last = null;
    }

    AbstractLinkedInsertList(Map<T, Node> nodeMap, Iterable<T> elements) {
        this.nodes = nodeMap;
        Node node = null;

        Iterator<T> iterator = elements.iterator();
        if (iterator.hasNext()) {
            T element = iterator.next();
            this.first = node = new Node(element, null, null);
            this.nodes.put(element, node);

            while (iterator.hasNext()) {
                element = iterator.next();
                Node prevNode = node;
                node = new Node(element, null, prevNode);
                prevNode.next = node;
                this.nodes.put(element, node);
            }
        }

        this.last = node;
    }

    private Node nodeByElement(Object element) {
        return nodes.get(element);
    }

    private Node nodeByIndex(int index) {
        int listSize = size();
        if (index < 0 || index >= listSize) {
            throw new IndexOutOfBoundsException("Index: " + index + " Size: " + listSize);
        }

        if (index < listSize / 2) {
            Node node = first;
            for (int i = 0; i < index; i++) {
                node = node.next;
            }
            return node;
        } else {
            Node node = last;
            for (int i = listSize - 1; i > index; i--) {
                node = node.prev;
            }
            return node;
        }
    }

    // Methods that implement InsertList

    @Override
    public T getFirst() {
        if (first == null) {
            throw new NoSuchElementException();
        } else {
            return first.element;
        }
    }

    @Override
    public T getLast() {
        if (last == null) {
            throw new NoSuchElementException();
        } else {
            return last.element;
        }
    }

    @Override
    public T getNext(T element) {
        Node node = nodeByElement(element);
        if (node == null) {
            throw new NoSuchElementException();
        } else {
            Node next = node.next;
            return next == null ? null : next.element;
        }
    }

    @Override
    public T getPrevious(T element) {
        Node node = nodeByElement(element);
        if (node == null) {
            throw new NoSuchElementException();
        } else {
            Node prev = node.prev;
            return prev == null ? null : prev.element;
        }
    }

    @Override
    public void insertAfter(T location, T element) {
        Node locationNode = nodeByElement(location);
        if (locationNode == null) {
            throw new NoSuchElementException();
        } else {
            new Node(element).insertAfter(locationNode);
        }
    }

    @Override
    public void insertAfter(T location, Collection<T> elements) {
        if (elements.isEmpty()) {
            return;
        }

        Iterator<T> iterator = elements.iterator();
        Node first = new Node(iterator.next());
        Node last = first;
        while (iterator.hasNext()) {
            Node node = new Node(iterator.next());
            last.next = node;
            node.prev = last;
            last = node;
        }

        Node locationNode = nodeByElement(location);
        if (locationNode == null) {
            throw new NoSuchElementException();
        } else {
            if (locationNode.next == null) {
                this.last = last;
            } else {
                locationNode.next.prev = last;
                last.next = locationNode.next;
            }

            first.prev = locationNode;
            locationNode.next = first;
        }
    }

    @Override
    public void insertBefore(T location, T element) {
        Node locationNode = nodeByElement(location);
        if (locationNode == null) {
            throw new NoSuchElementException();
        } else {
            new Node(element).insertBefore(locationNode);
        }
    }

    @Override
    public void insertBefore(T location, Collection<T> elements) {
        if (elements.isEmpty()) {
            return;
        }

        Iterator<T> iterator = elements.iterator();
        Node first = new Node(iterator.next());
        Node last = first;
        while (iterator.hasNext()) {
            Node node = new Node(iterator.next());
            last.next = node;
            node.prev = last;
            last = node;
        }

        Node locationNode = nodeByElement(location);
        if (locationNode == null) {
            throw new NoSuchElementException();
        } else {
            if (locationNode.prev == null) {
                this.first = first;
            } else {
                locationNode.prev.next = first;
                first.prev = locationNode.prev;
            }

            last.next = locationNode;
            locationNode.prev = last;
        }
    }

    // Methods that implement java.util.List

    @Override
    public T get(int index) {
        return nodeByIndex(index).element;
    }

    @Override
    public T set(int index, T element) {
        return nodeByIndex(index).setElement(element);
    }

    @Override
    public boolean add(T element) {
        Node node = new Node(element);
        node.insertAfter(last);
        return true;
    }

    @Override
    public void add(int index, T element) {
        if (index == size()) {
            add(element);
        } else {
            Node node = new Node(element);
            node.insertAfter(nodeByIndex(index));
        }
    }

    @Override
    public boolean remove(Object obj) {
        Node node = nodeByElement(obj);
        if (node == null) {
            return false;
        } else {
            node.remove();
            return true;
        }
    }

    @Override
    public T remove(int index) {
        Node node = nodeByIndex(index);
        node.remove();
        return node.element;
    }

    @Override
    public int size() {
        return this.nodes.size();
    }

    @Override
    public void clear() {
        first = null;
        last = null;
        nodes.clear();
    }

    @Override
    public boolean contains(Object o) {
        return nodes.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iter(first);
    }

    @Override
    public Iterator<T> iterator(T element) {
        return new Iter(nodeByElement(element));
    }

    @Override
    public Iterator<T> iterator(T start, T end) {
        return new RangeIter(nodeByElement(start), nodeByElement(end));
    }

    @Override
    public ListIterator<T> listIterator() {
        return new ListIter(0, first);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        Node node = index == size() ? null : nodeByIndex(index);
        return new ListIter(index, node);
    }

    private class Node {
        private T element;
        private Node next;
        private Node prev;

        public Node(T element) {
            this(element, null, null);
        }

        public Node(T element, Node next, Node prev) {
            if (nodes.containsKey(element)) {
                throw new IllegalArgumentException("List already contains element");
            }

            this.element = element;
            this.next = next;
            this.prev = prev;
            nodes.put(element, this);
        }

        /**
         * Change the element contained within this node.
         *
         * @param newElement the element to be set
         * @return the old element of this node.
         */
        public T setElement(T newElement) {
            T oldElement = this.element;
            if (newElement != oldElement) {
                if (nodes.containsKey(newElement)) {
                    throw new IllegalArgumentException("List already contains element");
                }

                nodes.remove(oldElement);
                nodes.put(newElement, this);
                this.element = newElement;
            }
            return oldElement;
        }

        /**
         * Remove this node from the list.
         */
        public void remove() {
            if (prev == null) {
                AbstractLinkedInsertList.this.first = next;
            } else {
                prev.next = next;
            }

            if (next == null) {
                AbstractLinkedInsertList.this.last = prev;
            } else {
                next.prev = prev;
            }

            nodes.remove(this);
        }

        /**
         * Insert this node after another one.
         *
         * @param location insert after that node
         */
        public void insertAfter(Node location) {
            if (location == null) {
                first = this;
                last = this;
            } else {
                if (location.next == null) {
                    last = this;
                } else {
                    location.next.prev = this;
                    next = location.next;
                }

                location.next = this;
                this.prev = location;
            }
        }

        /**
         * Insert this node before another one.
         *
         * @param location insert after that node
         */
        public void insertBefore(Node location) {
            if (location == null) {
                first = this;
                last = this;
            } else {
                if (location.prev == null) {
                    first = this;
                } else {
                    location.prev.next = this;
                    prev = location.prev;
                }

                location.prev = this;
                this.next = location;
            }
        }
    }

    private class Iter implements Iterator<T> {
        private Node node;

        public Iter(Node node) {
            this.node = node;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public T next() {
            T element = node.element;
            node = node.next;
            return element;
        }
    }

    private class RangeIter implements Iterator<T> {
        private Node node;
        private final Node end;

        public RangeIter(Node start, Node end) {
            this.node = start;
            this.end = end;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public T next() {
            T element = node.element;

            if (node == end) {
                node = null;
            } else if ((node = node.next) == null) {
                // We've reached the last element of the list but did not yet see the 'end' node.
                throw new IllegalStateException("Illegal Range");
            }

            return element;
        }
    }

    private class ListIter implements ListIterator<T> {
        private int nextIndex;
        private Node next;
        private Node lastReturned;

        private ListIter(int index, Node node) {
            this.nextIndex = index;
            this.next = node;
        }

        @Override
        public boolean hasNext() {
            return nextIndex < size();
        }

        @Override
        public T next() {
            if (next == null) {
                throw new NoSuchElementException();
            } else {
                this.lastReturned = next;
                this.next = this.next.next;
                nextIndex += 1;
                return this.lastReturned.element;
            }
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public T previous() {
            if (hasPrevious()) {
                this.lastReturned = this.next = (this.next == null ? last : this.next.prev);
                this.nextIndex -= 1;
                return this.lastReturned.element;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            } else {
                Node next = this.lastReturned.next;

                this.lastReturned.remove();

                if (this.next == this.lastReturned) {
                    this.next = next;
                } else {
                    this.nextIndex -= 1;
                }

                this.lastReturned = null;
            }
        }

        @Override
        public void set(T element) {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            } else {
                this.lastReturned.setElement(element);
            }
        }

        @Override
        public void add(T t) {
            if (this.next == null) {
                new Node(t).insertAfter(last);
            } else {
                new Node(t).insertBefore(this.next);
            }

            this.lastReturned = null;
        }
    }
}
