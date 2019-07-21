package me.aki.tactical.stack.utils.analysis;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A stack implemented as immutable single linked list to allow constant time copies.
 */
public class Stack<T> {
    public static class Mutable<T> extends Stack<T> {
        public Mutable() {
            this(Optional.empty(), 0);
        }

        /**
         * Create a stack from a list of elements.
         * The first provided element is the first element to be popped.
         *
         * @param values the values on the stack
         */
        public Mutable(T... values) {
            this(Optional.empty(), values.length);
            for (int i = values.length - 1; i >= 0; i--) {
                this.head = Optional.of(new Stack<T>.Node(values[i], this.head));
            }
        }

        private Mutable(Optional<Stack<T>.Node> head, int size) {
            super(head, size);
        }

        /**
         * Push a value onto the stack
         *
         * @param value
         */
        public void push(T value) {
            this.head = Optional.of(new Stack<T>.Node(value, this.head));
            this.size += 1;
        }

        /**
         * Get and remove the upper value on the stack.
         *
         * @return removed upper value
         * @throws NoSuchElementException the stack was empty
         */
        public T pop() {
            Stack<T>.Node head = this.head.orElseThrow(NoSuchElementException::new);
            this.head = head.tail;
            this.size -= 1;
            return head.value;
        }

        /**
         * Clone the state of another stack into this stack.
         *
         * @param other stack to be loaded
         */
        public void loadFrom(Stack<T> other) {
            this.head = other.head;
            this.size = other.size;
        }

        /**
         * Delete all values on the stack
         */
        public void clear() {
            this.size = 0;
            this.head = Optional.empty();
        }
    }

    /**
     * An immutable snapshot of a stack state
     */
    public static class Immutable<T> extends Stack<T> {
        public Immutable() {
            this(Optional.empty(), 0);
        }

        /**
         * Create a stack from a list of elements.
         * The first provided element is the first element to be popped.
         *
         * @param values the values on the stack
         */
        public Immutable(T... values) {
            this(Optional.empty(), values.length);
            for (int i = values.length - 1; i >= 0; i--) {
                this.head = Optional.of(new Stack<T>.Node(values[i], this.head));
            }
        }

        public Immutable(Optional<Stack<T>.Node> head, int size) {
            super(head, size);
        }
    }

    /**
     * The most upper value of the stack.
     */
    protected Optional<Node> head;

    /**
     * Amount of values on the stack
     */
    protected int size;

    private Stack(Optional<Node> head, int size) {
        this.head = head;
        this.size = size;
    }

    /**
     * Get, but do not remove the upper value on the stack.
     *
     * @return upper value on the stack
     * @throws NoSuchElementException the stack was empty
     */
    public T peek() {
        return this.head.orElseThrow(NoSuchElementException::new).value;
    }

    /**
     * Get the most upper value on the stack if present.
     *
     * @return upper value on the stack
     */
    public Optional<T> peekOpt() {
        return this.head.map(node -> node.value);
    }

    /**
     * Copy the n must upper values into an array.
     * The most upper value will be at index zero.
     *
     * @param amount of values to peek
     * @return an array of peeked values
     */
    @SuppressWarnings("unchecked")
    public T[] peek(int amount) {
        T[] array = (T[]) new Object[amount];
        Optional<Node> nodeOpt = this.head;
        for (int i = 0; i < amount; i++) {
            Node node = nodeOpt.orElseThrow(NoSuchElementException::new);
            array[i] = node.value;
            nodeOpt = node.tail;
        }
        return array;
    }

    /**
     * Iterator that peeks values starting from the most upper value.
     *
     * @return iterator over the stack
     */
    public Iterator<T> peekIterator() {
        return new Iterator<>() {
            private Optional<Node> nodeOpt = Stack.this.head;

            @Override
            public boolean hasNext() {
                return nodeOpt.isPresent();
            }

            @Override
            public T next() {
                Node node = nodeOpt.orElseThrow(NoSuchElementException::new);
                this.nodeOpt = node.tail;
                return node.value;
            }
        };
    }

    /**
     * Create a constant time immutable copy of this stack.
     *
     * @return copy of this stack
     */
    public Stack.Immutable<T> toImmutable() {
        return this instanceof Stack.Immutable ? (Stack.Immutable<T>) this :
                new Stack.Immutable<>(this.head, this.size);
    }

    /**
     * Create a constant time mutable copy of this stack.
     *
     * @return copy of this stack
     */
    public Stack.Mutable<T> toMutable() {
        return new Stack.Mutable<>(this.head, this.size);
    }

    /**
     * Get the amount of values values on the stack.
     * Long and Double values are not counted as one values.
     *
     * @return size of the stack
     */
    public int getSize() {
        return size;
    }

    public boolean isEqual(Stack stack) {
        return this.size == stack.size &&
                Objects.equals(this.head, stack.head);
    }

    public Stream<T> stream() {
        return Stream.iterate(this.head, Optional::isPresent, nodeOpt -> nodeOpt.flatMap(node -> node.tail))
                .flatMap(node -> node.map(x -> x.value).stream());
    }

    public List<T> toList() {
        return stream().collect(Collectors.toList());
    }

    private class Node {
        private final T value;
        private final Optional<Node> tail;

        public Node(T value, Optional<Node> tail) {
            this.value = value;
            this.tail = tail;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(value, node.value) &&
                    Objects.equals(tail, node.tail);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, tail);
        }
    }
}
