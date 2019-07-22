package me.aki.tactical.dex;

import me.aki.tactical.core.Path;
import me.aki.tactical.core.util.RWCell;
import me.aki.tactical.dex.insn.Instruction;
import me.aki.tactical.dex.insn.MoveExceptionInstruction;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Catch exceptions within a range of instructions and branch to certain {@link Instruction Instructions}
 * depending on the kind of the caught exception.
 */
public class TryCatchBlock {
    /**
     * The start of a instruction range within this try/catch block catches exception.
     */
    private Instruction start;

    /**
     * The (inclusive) end of a instruction range within this try/catch block catches exception.
     */
    private Instruction end;

    /**
     * The kind of exceptions that get caught and how they are handled
     */
    private List<Handler> handlers;

    public TryCatchBlock(Instruction start, Instruction end, List<Handler> handlers) {
        this.start = start;
        this.end = end;
        this.handlers = handlers;
    }

    public Instruction getStart() {
        return start;
    }

    public void setStart(Instruction start) {
        this.start = start;
    }

    public RWCell<Instruction> getStartCell() {
        return RWCell.of(this::getStart, this::setStart, Instruction.class);
    }

    public Instruction getEnd() {
        return end;
    }

    public void setEnd(Instruction end) {
        this.end = end;
    }

    public RWCell<Instruction> getEndCell() {
        return RWCell.of(this::getEnd, this::setEnd, Instruction.class);
    }

    public List<Handler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TryCatchBlock that = (TryCatchBlock) o;
        return Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(handlers, that.handlers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, handlers);
    }

    /**
     * Handle one kind of exceptions by branching to a certain instruction
     */
    public static class Handler {
        /**
         * The type of exceptions that is caught or empty to catch all exceptions.
         */
        private Optional<Path> exception;

        /**
         * Start of the code that handles caught exceptions.
         * This is either a {@link MoveExceptionInstruction} or the exception is ignored.
         */
        private Instruction handler;

        public Handler(Optional<Path> exception, Instruction handler) {
            this.exception = exception;
            this.handler = handler;
        }

        public Optional<Path> getException() {
            return exception;
        }

        public void setException(Optional<Path> exception) {
            this.exception = exception;
        }

        public Instruction getHandler() {
            return handler;
        }

        public void setHandler(Instruction handler) {
            this.handler = handler;
        }

        public RWCell<Instruction> getHandlerCell() {
            return RWCell.of(this::getHandler, this::setHandler, Instruction.class);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Handler handler1 = (Handler) o;
            return Objects.equals(exception, handler1.exception) &&
                    Objects.equals(handler, handler1.handler);
        }

        @Override
        public int hashCode() {
            return Objects.hash(exception, handler);
        }
    }
}
