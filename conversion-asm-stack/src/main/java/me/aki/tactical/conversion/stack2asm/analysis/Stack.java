package me.aki.tactical.conversion.stack2asm.analysis;

import me.aki.tactical.conversion.asm2stack.AsmInsnReader;

import java.util.Iterator;
import java.util.Optional;

/**
 * Emulate the types of values on a JVM stack.
 *
 * Its implemented as immutable single linked list allows constant time copies.
 */
public class Stack {
    /**
     * The most upper value of the stack.
     */
    private Optional<Node> head;

    /**
     * ammount of values on the stack
     */
    private int size;

    /**
     * Create an empty stack
     */
    public Stack() {
        this(Optional.empty(), 0);
    }

    private Stack(Optional<Node> head, int size) {
        this.head = head;
    }

    /**
     * Push a type onto the stack
     *
     * @param type
     */
    public void push(JvmType type) {
        this.head = Optional.of(new Node(type, this.head));
        this.size += 1;
    }

    /**
     * Get and remove the upper type on the stack.
     *
     * @return removed upper type
     * @throws StackUnderflowException the stack was empty
     */
    public JvmType pop() {
        Node head = this.head.orElseThrow(StackUnderflowException::new);
        this.head = head.tail;
        this.size -= 1;
        return head.type;
    }

    /**
     * Require that a certain type is on top of the stack and drop it.
     *
     * @param type that must be on top of the stack
     * @throws StackUnderflowException the stack was empty
     * @throws IllegalStateException the requirement does not match
     */
    public void popRequire(JvmType type) {
        JvmType actual = pop();
        if (type != actual) {
            throw new StackStateException(type, actual);
        }
    }

    /**
     * Get, but do not remove the upper type on the stack.
     *
     * @return upper type on the stack
     * @throws StackUnderflowException the stack was empty
     */
    public JvmType peek() {
        return this.head.orElseThrow(StackUnderflowException::new).type;
    }

    /**
     * Copy the n must upper values into an array.
     * The most upper value will be at index zero.
     *
     * @param ammount of values to peek
     * @return an array of peeked values
     */
    public JvmType[] peek(int ammount) {
        JvmType[] array = new JvmType[ammount];
        Optional<Node> nodeOpt = this.head;
        for (int i = 0; i < ammount; i++) {
            Node node = nodeOpt.orElseThrow(StackUnderflowException::new);
            array[i] = node.type;
            nodeOpt = node.tail;
        }
        return array;
    }

    /**
     * Iterator that peeks values starting from the most upper value.
     *
     * @return iterator over the stack
     */
    public Iterator<JvmType> peekIterator() {
        return new Iterator<>() {
            private Optional<Node> nodeOpt = Stack.this.head;

            @Override
            public boolean hasNext() {
                return nodeOpt.isPresent();
            }

            @Override
            public JvmType next() {
                Node node = nodeOpt.orElseThrow(StackUnderflowException::new);
                this.nodeOpt = node.tail;
                return node.type;
            }
        };
    }

    /**
     * Get the most upper type on the stack if present.
     *
     * @return upper type on the stack
     */
    public Optional<JvmType> peekOpt() {
        return this.head.map(node -> node.type);
    }

    /**
     * Create a constant time copy of this stack.
     *
     * @return copy of this stack
     */
    public Stack copy() {
        return new Stack(this.head, this.size);
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
     * Get the amount of values types on the stack.
     * Long and Double values are not counted as one values.
     *
     * @return size of the stack
     */
    public int getSize() {
        return size;
    }

    public JvmType[] toArray() {
        JvmType[] array = new JvmType[size];

        Optional<Node> nodeOpt = this.head;
        int index = array.length - 1;
        while (nodeOpt.isPresent()) {
            Node node = nodeOpt.get();
            array[index--] = node.type;
            nodeOpt = node.tail;
        }

        return array;
    }

    /**
     * Delete all types on the stack
     */
    public void clear() {
        this.size = 0;
        this.head = Optional.empty();
    }

    public static class Node {
        private final JvmType type;
        private final Optional<Node> tail;

        public Node(JvmType type, Optional<Node> tail) {
            this.type = type;
            this.tail = tail;
        }
    }

    public static class StackUnderflowException extends RuntimeException {}

    public static class StackStateException extends RuntimeException {
        public StackStateException(JvmType expected, JvmType actual) {
            super("Wrong type on Stack: expeced: " + expected + ", got: " + actual);
        }
    }
}
