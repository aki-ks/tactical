package me.aki.tactical.conversion.stackasm.analysis;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A stack implemented as immutable single linked list to allow constant time copies.
 */
public class Stack<T> {
    public static class Mutable<T> extends Stack<T> {
        public Mutable() {}

        public Mutable(Optional<Stack<T>.Node> head, int size) {
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
         * @throws StackUnderflowException the stack was empty
         */
        public T pop() {
            Stack<T>.Node head = this.head.orElseThrow(StackUnderflowException::new);
            this.head = head.tail;
            this.size -= 1;
            return head.value;
        }

        /**
         * Clone the state of another stack into this stack.
         *
         * @param other stack to be loaded
         */
        public void loadFrom(Stack other) {
            this.head = other.head;
            this.size = other.size;
        }

        /**
         * Delete all types on the stack
         */
        public void clear() {
            this.size = 0;
            this.head = Optional.empty();
        }
    }

    /**
     * A immutable snapshot of a stack state
     */
    public static class Immutable<T> extends Stack<T> {
        public Immutable() {}

        public Immutable(Optional<Stack<T>.Node> head, int size) {
            super(head, size);
        }
    }

    /**
     * The most upper value of the stack.
     */
    protected Optional<Node> head;

    /**
     * amount of values on the stack
     */
    protected int size;

    /**
     * Create an empty stack
     */
    public Stack() {
        this(Optional.empty(), 0);
    }

    private Stack(Optional<Node> head, int size) {
        this.head = head;
        this.size = size;
    }

    /**
     * Get, but do not remove the upper value on the stack.
     *
     * @return upper value on the stack
     * @throws StackUnderflowException the stack was empty
     */
    public T peek() {
        return this.head.orElseThrow(StackUnderflowException::new).value;
    }

    /**
     * Copy the n must upper values into an array.
     * The most upper value will be at index zero.
     *
     * @param ammount of values to peek
     * @return an array of peeked values
     */
    public T[] peek(int ammount) {
        T[] array = (T[]) new Object[ammount];
        Optional<Node> nodeOpt = this.head;
        for (int i = 0; i < ammount; i++) {
            Node node = nodeOpt.orElseThrow(StackUnderflowException::new);
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
                Node node = nodeOpt.orElseThrow(StackUnderflowException::new);
                this.nodeOpt = node.tail;
                return node.value;
            }
        };
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
     * Create a constant time immutable copy of this stack.
     *
     * @return copy of this stack
     */
    public Stack.Immutable<T> immutableCopy() {
        return this instanceof Stack.Immutable ? (Stack.Immutable<T>) this :
                new Stack.Immutable<>(this.head, this.size);
    }

    /**
     * Create a constant time mutable copy of this stack.
     *
     * @return copy of this stack
     */
    public Stack.Mutable<T> mutableCopy() {
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
        return size == stack.size &&
                Objects.equals(head, stack.head);
    }

    @SuppressWarnings("unchecked")
    public <U> U[] toArray(Function<Integer, U[]> newArray) {
        U[] obj = newArray.apply(this.size);
        Optional<Node> nodeOpt = this.head;
        int i = this.size - 1;
        while (nodeOpt.isPresent()) {
            Node node = nodeOpt.get();
            obj[i--] = (U) node.value;
            nodeOpt = node.tail;
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    public Stream<T> stream() {
        return (Stream<T>) Arrays.stream(toArray(Object[]::new));
    }

    @SuppressWarnings("unchecked")
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

    public static class StackUnderflowException extends RuntimeException {}
}
