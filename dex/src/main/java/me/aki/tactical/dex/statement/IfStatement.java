package me.aki.tactical.dex.statement;

import me.aki.tactical.dex.Register;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Branch to another location in code if a certain condition applies to two values.
 */
public class IfStatement implements BranchStatement {
    /**
     * How to compared the two values.
     */
    private Comparison comparison;

    /**
     * Register containing the first value to be compared.
     */
    private Register op1;

    /**
     * Register containing the second value to be compared.
     *
     * If the register is not present, the first value is compared against zero.
     */
    private Optional<Register> op2;

    /**
     * Branch to this location if the condition applied
     */
    private Statement target;

    public IfStatement(Comparison comparison, Register op1, Optional<Register> op2, Statement target) {
        this.comparison = comparison;
        this.op1 = op1;
        this.op2 = op2;
        this.target = target;
    }

    public Comparison getComparison() {
        return comparison;
    }

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    public Register getOp1() {
        return op1;
    }

    public void setOp1(Register op1) {
        this.op1 = op1;
    }

    public Optional<Register> getOp2() {
        return op2;
    }

    public void setOp2(Optional<Register> op2) {
        this.op2 = op2;
    }

    public Statement getTarget() {
        return target;
    }

    public void setTarget(Statement target) {
        this.target = target;
    }

    @Override
    public List<Statement> getBranchTargets() {
        return List.of(target);
    }

    @Override
    public List<Register> getReadRegisters() {
        return Stream.concat(Stream.of(op1), op2.stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<Register> getWrittenRegister() {
        return Optional.empty();
    }

    public enum Comparison {
        EQUAL, NON_EQUAL, LESS_THAN, LESS_EQUAL, GREATER_THAN, GREATER_EQUAL;

        /**
         * Get the comparison that would result in the opposite value.
         *
         * @return get the opposite comparison
         */
        public Comparison opposite() {
            switch (this) {
                case EQUAL: return NON_EQUAL;
                case NON_EQUAL: return EQUAL;
                case LESS_THAN: return GREATER_EQUAL;
                case LESS_EQUAL: return GREATER_THAN;
                case GREATER_THAN: return LESS_EQUAL;
                case GREATER_EQUAL: return LESS_THAN;
                default: throw new AssertionError();
            }
        }
    }
}
