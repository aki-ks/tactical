package me.aki.tactical.stack;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.typeannotation.ExceptionTypeAnnotation;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.stack.insn.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a handler for exceptions that occur within a certain range of instructions.
 */
public class TryCatchBlock {
    /**
     * First instruction in the range of instructions protected by this block
     */
    private Instruction first;

    /**
     * Last instruction in the range of instructions protected by this block.
     */
    private Instruction last;

    /**
     * The jvm branches to this location if an exception was caught.
     */
    private Instruction handler;

    /**
     * Kind of exceptions that get caught by this block or empty
     * to catch any exception (see <tt>finally</tt> blocks).
     */
    private Optional<Path> exceptionType;

    /**
     * Type annotations of the exception type.
     */
    private List<ExceptionTypeAnnotation> typeAnnotations = new ArrayList<>();

    public TryCatchBlock(Instruction first, Instruction last, Instruction handler, Optional<Path> exceptionType) {
        this.first = first;
        this.last = last;
        this.handler = handler;
        this.exceptionType = exceptionType;
    }

    public Instruction getFirst() {
        return first;
    }

    public void setFirst(Instruction first) {
        this.first = first;
    }

    public Cell<Instruction> getFirstCell() {
        return Cell.of(this::getFirst, this::setFirst, Instruction.class);
    }

    public Instruction getLast() {
        return last;
    }

    public void setLast(Instruction last) {
        this.last = last;
    }

    public Cell<Instruction> getLastCell() {
        return Cell.of(this::getLast, this::setLast, Instruction.class);
    }

    public Instruction getHandler() {
        return handler;
    }

    public void setHandler(Instruction handler) {
        this.handler = handler;
    }

    public Cell<Instruction> getHandlerCell() {
        return Cell.of(this::getHandler, this::setHandler, Instruction.class);
    }

    public Optional<Path> getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(Optional<Path> exceptionType) {
        this.exceptionType = exceptionType;
    }

    public List<ExceptionTypeAnnotation> getTypeAnnotations() {
        return typeAnnotations;
    }

    public void setTypeAnnotations(List<ExceptionTypeAnnotation> typeAnnotations) {
        this.typeAnnotations = typeAnnotations;
    }
}
