package me.aki.tactical.stack;

import me.aki.tactical.core.Body;
import me.aki.tactical.stack.insn.Instruction;
import me.aki.tactical.stack.insn.StoreInsn;

import java.util.List;
import java.util.Optional;

public class StackBody implements Body {
    /**
     * All locals of this method (including argument and this local).
     */
    private List<Local> locals;

    /**
     * Local that initially contains the "this" value for non-static method.
     *
     * Note that other values can be assigned to this local with a {@link StoreInsn}.
     */
    private Optional<Local> thisLocal;

    /**
     * Locals that initially contain the parameter values.
     *
     * Note that other values can be assigned to these local with a {@link StoreInsn}.
     */
    private List<Local> parameterLocals;

    private List<Instruction> instructions;

    public StackBody(List<Local> locals, Optional<Local> thisLocal, List<Local> parameterLocals,
                     List<Instruction> instructions) {
        this.locals = locals;
        this.thisLocal = thisLocal;
        this.parameterLocals = parameterLocals;
        this.instructions = instructions;
    }

    public List<Local> getLocals() {
        return locals;
    }

    public void setLocals(List<Local> locals) {
        this.locals = locals;
    }

    public Optional<Local> getThisLocal() {
        return thisLocal;
    }

    public void setThisLocal(Optional<Local> thisLocal) {
        this.thisLocal = thisLocal;
    }

    public List<Local> getParameterLocals() {
        return parameterLocals;
    }

    public void setParameterLocals(List<Local> parameterLocals) {
        this.parameterLocals = parameterLocals;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }
}
