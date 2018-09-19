package me.aki.tactical.stack;

import me.aki.tactical.core.Body;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.LocalVariableTypeAnnotation;
import me.aki.tactical.stack.insn.Instruction;
import me.aki.tactical.stack.insn.StoreInsn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StackBody implements Body {
    /**
     * All locals of this method.
     *
     * This includes also e.g. the "this", parameter and catch-block local.
     */
    private List<Local> locals = new ArrayList<>();

    /**
     * Local that initially contains the "this" value for non-static method.
     *
     * Note that other values can be assigned to this local with a {@link StoreInsn}.
     */
    private Optional<Local> thisLocal = Optional.empty();

    /**
     * Locals that initially contain the parameter values.
     *
     * Note that other values can be assigned to these local with a {@link StoreInsn}.
     */
    private List<Local> parameterLocals = new ArrayList<>();

    /**
     * The instructions of this method
     */
    private List<Instruction> instructions = new ArrayList<>();

    /**
     * The try-catch-blocks of this method.
     */
    private List<TryCatchBlock> tryCatchBlocks = new ArrayList<>();

    /**
     * Debug info about local variables that existed in source code.
     */
    private List<LocalVariable> localVariables = new ArrayList<>();

    /**
     * Annotations of the types of local variables
     */
    private List<LocalVariableAnnotation> localVariableAnnotations = new ArrayList<>();

    /**
     * Debug info about the line numbers of the source file.
     */
    private List<LineNumber> lineNumbers = new ArrayList<>();

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

    public List<TryCatchBlock> getTryCatchBlocks() {
        return tryCatchBlocks;
    }

    public void setTryCatchBlocks(List<TryCatchBlock> tryCatchBlocks) {
        this.tryCatchBlocks = tryCatchBlocks;
    }

    public List<LocalVariable> getLocalVariables() {
        return localVariables;
    }

    public void setLocalVariables(List<LocalVariable> localVariables) {
        this.localVariables = localVariables;
    }

    public List<LocalVariableAnnotation> getLocalVariableAnnotations() {
        return localVariableAnnotations;
    }

    public void setLocalVariableAnnotations(List<LocalVariableAnnotation> localVariableAnnotations) {
        this.localVariableAnnotations = localVariableAnnotations;
    }

    public List<LineNumber> getLineNumbers() {
        return lineNumbers;
    }

    public void setLineNumbers(List<LineNumber> lineNumbers) {
        this.lineNumbers = lineNumbers;
    }

    /**
     * Debug information about a local variable that existed in source.
     */
    public static class LocalVariable {
        /**
         * Name of the local variable
         */
        private String name;

        /**
         * Type of this local variable
         */
        private Type type;

        /**
         * Type of this local variable with type variables.
         */
        private Optional<String> signature;

        /**
         * First instruction in the range of instructions within this
         * local variable existed in source.
         */
        private Instruction start;

        /**
         * Last instruction in the range of instructions within this
         * local variable existed in source.
         */
        private Instruction end;

        /**
         * Local that corresponds to the local variable in source.
         */
        private Local local;

        public LocalVariable(String name, Type type, Optional<String> signature, Instruction start,
                             Instruction end, Local local) {
            this.name = name;
            this.type = type;
            this.signature = signature;
            this.start = start;
            this.end = end;
            this.local = local;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Optional<String> getSignature() {
            return signature;
        }

        public void setSignature(Optional<String> signature) {
            this.signature = signature;
        }

        public Instruction getStart() {
            return start;
        }

        public void setStart(Instruction start) {
            this.start = start;
        }

        public Instruction getEnd() {
            return end;
        }

        public void setEnd(Instruction end) {
            this.end = end;
        }

        public Local getLocal() {
            return local;
        }

        public void setLocal(Local local) {
            this.local = local;
        }
    }

    public static class LocalVariableAnnotation {
        /**
         * Annotation of the local variable
         */
        private LocalVariableTypeAnnotation annotation;

        /**
         * Code ranges where this local has the annotation.
         */
        private List<Location> locations;

        public LocalVariableAnnotation(LocalVariableTypeAnnotation annotation, List<Location> locations) {
            this.annotation = annotation;
            this.locations = locations;
        }

        public LocalVariableTypeAnnotation getAnnotation() {
            return annotation;
        }

        public void setAnnotation(LocalVariableTypeAnnotation annotation) {
            this.annotation = annotation;
        }

        public List<Location> getLocations() {
            return locations;
        }

        public void setLocations(List<Location> locations) {
            this.locations = locations;
        }

        /**
         * A range of instructions where a local correlates to a local variable in source.
         */
        public static class Location {
            /**
             * First instruction of the code range.
             */
            private Instruction start;

            /**
             * Last instruction of the code range.
             */
            private Instruction end;

            /**
             * Local that corresponds to the local variable within the range of instruction.
             */
            private Local local;

            public Location(Instruction start, Instruction end, Local local) {
                this.start = start;
                this.end = end;
                this.local = local;
            }

            public Instruction getStart() {
                return start;
            }

            public void setStart(Instruction start) {
                this.start = start;
            }

            public Instruction getEnd() {
                return end;
            }

            public void setEnd(Instruction end) {
                this.end = end;
            }

            public Local getLocal() {
                return local;
            }

            public void setLocal(Local local) {
                this.local = local;
            }
        }
    }

    /**
     * Debug information that relates instructions and their line number in sourcecode.
     */
    public static class LineNumber {
        /**
         * The line number of the corresponding statement in sourcecode.
         */
        private int line;

        /**
         * First instruction from that line in the sourcecode.
         */
        private Instruction instruction;

        public LineNumber(int line, Instruction instruction) {
            this.line = line;
            this.instruction = instruction;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public Instruction getInstruction() {
            return instruction;
        }

        public void setInstruction(Instruction instruction) {
            this.instruction = instruction;
        }
    }
}
