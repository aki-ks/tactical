package me.aki.tactical.stack;

import me.aki.tactical.core.type.ObjectType;
import me.aki.tactical.stack.insn.Instruction;

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
     * If an exception was caught, it gets stored in that local and the jvm branches
     * to the {@link TryCatchBlock#handler} instruction.
     */
    private Local exceptionLocal;

    /**
     * Kind of exceptions that get caught by this block
     * or empty to catch any exception.
     */
    private Optional<ObjectType> exceptionType;

    public TryCatchBlock(Instruction first, Instruction last, Instruction handler, Local exceptionLocal, Optional<ObjectType> exceptionType) {
        this.first = first;
        this.last = last;
        this.handler = handler;
        this.exceptionLocal = exceptionLocal;
        this.exceptionType = exceptionType;
    }

    public Instruction getFirst() {
        return first;
    }

    public void setFirst(Instruction first) {
        this.first = first;
    }

    public Instruction getLast() {
        return last;
    }

    public void setLast(Instruction last) {
        this.last = last;
    }

    public Instruction getHandler() {
        return handler;
    }

    public void setHandler(Instruction handler) {
        this.handler = handler;
    }

    public Local getExceptionLocal() {
        return exceptionLocal;
    }

    public void setExceptionLocal(Local exceptionLocal) {
        this.exceptionLocal = exceptionLocal;
    }

    public Optional<ObjectType> getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(Optional<ObjectType> exceptionType) {
        this.exceptionType = exceptionType;
    }
}
