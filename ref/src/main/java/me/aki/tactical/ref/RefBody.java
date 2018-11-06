package me.aki.tactical.ref;

import me.aki.tactical.core.Body;
import me.aki.tactical.core.type.Type;
import me.aki.tactical.core.typeannotation.LocalVariableTypeAnnotation;
import me.aki.tactical.core.util.Cell;
import me.aki.tactical.core.util.InsertList;
import me.aki.tactical.core.util.LinkedInsertList;
import me.aki.tactical.ref.stmt.AssignStmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RefBody implements Body {
    /**
     * All locals of this method.
     */
    private List<RefLocal> locals = new ArrayList<>();

    /**
     * Local that initially contains the "this" value for non-static method.
     *
     * Note that other values can be assigned to these local with a {@link AssignStmt}.
     */
    private Optional<RefLocal> thisLocal = Optional.empty();

    /**
     * Locals that initially contain the parameter values.
     *
     * Note that other values can be assigned to these local with a {@link AssignStmt}.
     */
    private List<RefLocal> argumentLocals = new ArrayList<>();

    /**
     * The statements of this method.
     */
    private InsertList<Statement> statements = new LinkedInsertList<>();

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

    public List<RefLocal> getLocals() {
        return locals;
    }

    public void setLocals(List<RefLocal> locals) {
        this.locals = locals;
    }

    public Optional<RefLocal> getThisLocal() {
        return thisLocal;
    }

    public void setThisLocal(Optional<RefLocal> thisLocal) {
        this.thisLocal = thisLocal;
    }

    public List<RefLocal> getArgumentLocals() {
        return argumentLocals;
    }

    public void setArgumentLocals(List<RefLocal> argumentLocals) {
        this.argumentLocals = argumentLocals;
    }

    public InsertList<Statement> getStatements() {
        return statements;
    }

    public void setStatements(InsertList<Statement> statements) {
        this.statements = statements;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefBody refBody = (RefBody) o;
        return Objects.equals(locals, refBody.locals) &&
                Objects.equals(thisLocal, refBody.thisLocal) &&
                Objects.equals(argumentLocals, refBody.argumentLocals) &&
                Objects.equals(statements, refBody.statements) &&
                Objects.equals(tryCatchBlocks, refBody.tryCatchBlocks) &&
                Objects.equals(localVariables, refBody.localVariables) &&
                Objects.equals(localVariableAnnotations, refBody.localVariableAnnotations) &&
                Objects.equals(lineNumbers, refBody.lineNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locals, thisLocal, argumentLocals, statements, tryCatchBlocks, localVariables, localVariableAnnotations, lineNumbers);
    }

    /**
     * Debug information about a local variable that existed in source.
     */
    public static class LocalVariable {
        /**
         * Name of the local variable.
         */
        private String name;

        /**
         * Type of this local variable.
         */
        private Type type;

        /**
         * Type of this local variable with type variables.
         */
        private Optional<String> signature;

        /**
         * First statement in the range of statements within this
         * local variable existed in source.
         */
        private Statement start;

        /**
         * Last statement in the range of statements within this
         * local variable existed in source.
         */
        private Statement end;

        /**
         * Local that corresponds to the local variable in source.
         */
        private RefLocal local;

        public LocalVariable(String name, Type type, Optional<String> signature, Statement start,
                             Statement end, RefLocal local) {
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

        public Statement getStart() {
            return start;
        }

        public void setStart(Statement start) {
            this.start = start;
        }

        public Cell<Statement> getStartCell() {
            return Cell.of(this::getStart, this::setStart, Statement.class);
        }

        public Statement getEnd() {
            return end;
        }

        public void setEnd(Statement end) {
            this.end = end;
        }

        public Cell<Statement> getEndCell() {
            return Cell.of(this::getEnd, this::setEnd, Statement.class);
        }

        public RefLocal getLocal() {
            return local;
        }

        public void setLocal(RefLocal local) {
            this.local = local;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocalVariable that = (LocalVariable) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(type, that.type) &&
                    Objects.equals(signature, that.signature) &&
                    start == that.start &&
                    end == that.end &&
                    Objects.equals(local, that.local);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type, signature, start, end, local);
        }
    }

    /**
     * Type Annotations of a local variables.
     */
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocalVariableAnnotation that = (LocalVariableAnnotation) o;
            return Objects.equals(annotation, that.annotation) &&
                    Objects.equals(locations, that.locations);
        }

        @Override
        public int hashCode() {
            return Objects.hash(annotation, locations);
        }

        /**
         * A range of statements where a local correlates to a local variable in source.
         */
        public static class Location {
            /**
             * First statement of the code range.
             */
            private Statement start;

            /**
             * Last statement of the code range.
             */
            private Statement end;

            /**
             * Local that corresponds to the local variable within the range of statement.
             */
            private RefLocal local;

            public Location(Statement start, Statement end, RefLocal local) {
                this.start = start;
                this.end = end;
                this.local = local;
            }

            public Statement getStart() {
                return start;
            }

            public void setStart(Statement start) {
                this.start = start;
            }

            public Cell<Statement> getStartCell() {
                return Cell.of(this::getStart, this::setStart, Statement.class);
            }

            public Statement getEnd() {
                return end;
            }

            public void setEnd(Statement end) {
                this.end = end;
            }

            public Cell<Statement> getEndCell() {
                return Cell.of(this::getEnd, this::setEnd, Statement.class);
            }

            public RefLocal getLocal() {
                return local;
            }

            public void setLocal(RefLocal local) {
                this.local = local;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Location location = (Location) o;
                return start == location.start &&
                        end == location.end &&
                        Objects.equals(local, location.local);
            }

            @Override
            public int hashCode() {
                return Objects.hash(start, end, local);
            }
        }
    }

    /**
     * Debug information that relates statements and their line number in sourcecode.
     */
    public static class LineNumber {
        /**
         * The line number of the corresponding statement in sourcecode.
         */
        private int line;

        /**
         * First statement from that line in the sourcecode.
         */
        private Statement statement;

        public LineNumber(int line, Statement statement) {
            this.line = line;
            this.statement = statement;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public Statement getStatement() {
            return statement;
        }

        public void setStatement(Statement statement) {
            this.statement = statement;
        }

        public Cell<Statement> getStatementCell() {
            return Cell.of(this::getStatement, this::setStatement, Statement.class);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LineNumber that = (LineNumber) o;
            return line == that.line &&
                    statement == that.statement;
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, statement);
        }
    }
}
