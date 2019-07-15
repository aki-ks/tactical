package me.aki.tactical.ref;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.typeannotation.ExceptionTypeAnnotation;
import me.aki.tactical.core.util.RWCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a handler for exceptions that occur within a certain range of instructions.
 */
public class TryCatchBlock {
    /**
     * First instruction in the range of instructions protected by this block
     */
    private Statement first;

    /**
     * Last instruction in the range of instructions protected by this block.
     */
    private Statement last;

    /**
     * The jvm branches to this location if an exception was caught.
     */
    private Statement handler;

    /**
     * Kind of exceptions that get caught by this block or empty
     * to catch any exception (see <tt>finally</tt> blocks).
     */
    private Optional<Path> exception;

    /**
     * Caught exceptions get stored in this {@link RefLocal}.
     */
    private RefLocal exceptionLocal;

    /**
     * Type annotations of the exception type.
     */
    private List<ExceptionTypeAnnotation> typeAnnotations = new ArrayList<>();

    public TryCatchBlock(Statement first, Statement last, Statement handler,
                         Optional<Path> exception, RefLocal exceptionLocal) {
        this.first = first;
        this.last = last;
        this.handler = handler;
        this.exception = exception;
        this.exceptionLocal = exceptionLocal;
    }

    public Statement getFirst() {
        return first;
    }

    public void setFirst(Statement first) {
        this.first = first;
    }

    public RWCell<Statement> getFirstCell() {
        return RWCell.of(this::getFirst, this::setFirst, Statement.class);
    }

    public Statement getLast() {
        return last;
    }

    public void setLast(Statement last) {
        this.last = last;
    }

    public RWCell<Statement> getLastCell() {
        return RWCell.of(this::getLast, this::setLast, Statement.class);
    }

    public Statement getHandler() {
        return handler;
    }

    public void setHandler(Statement handler) {
        this.handler = handler;
    }

    public RWCell<Statement> getHandlerCell() {
        return RWCell.of(this::getHandler, this::setHandler, Statement.class);
    }

    public Optional<Path> getException() {
        return exception;
    }

    public void setException(Optional<Path> exception) {
        this.exception = exception;
    }

    public RefLocal getExceptionLocal() {
        return exceptionLocal;
    }

    public void setExceptionLocal(RefLocal exceptionLocal) {
        this.exceptionLocal = exceptionLocal;
    }

    public RWCell<RefLocal> getExceptionLocalCell() {
        return RWCell.of(this::getExceptionLocal, this::setExceptionLocal, RefLocal.class);
    }

    public List<ExceptionTypeAnnotation> getTypeAnnotations() {
        return typeAnnotations;
    }

    public void setTypeAnnotations(List<ExceptionTypeAnnotation> typeAnnotations) {
        this.typeAnnotations = typeAnnotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TryCatchBlock that = (TryCatchBlock) o;
        return Objects.equals(first, that.first) &&
                Objects.equals(last, that.last) &&
                Objects.equals(handler, that.handler) &&
                Objects.equals(exception, that.exception) &&
                Objects.equals(exceptionLocal, that.exceptionLocal) &&
                Objects.equals(typeAnnotations, that.typeAnnotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, last, handler, exception, exceptionLocal, typeAnnotations);
    }
}
